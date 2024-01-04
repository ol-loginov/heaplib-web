package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.*
import com.github.ol_loginov.heaplibweb.hprof.views.InstanceDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.ObjectArrayDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.PrimitiveArrayDumpView
import com.github.ol_loginov.heaplibweb.repository.heap.*
import com.github.ol_loginov.heaplibweb.support.pretty
import com.github.ol_loginov.heaplibweb.support.use
import org.springframework.transaction.support.TransactionOperations

internal class LoadInstances(
    private val hprof: HprofFile,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classDumpLookup: ClassDumpLookup,
    private val classCountCollector: ClassCountCollector,
    private val javaRootCollector: JavaRootCollector,
    private val fieldEntityLookup: FieldEntityLookup,
    private val loadPrimitiveArrayItems: Boolean
) : Task {
    private var passed = 0
    private var instancesLoaded = 0
    private var primitiveArrays = 0
    private var objectArrays = 0

    override fun getText() = "import instances: ${passed.pretty()} dumps (instances=${instancesLoaded.pretty()}, primitive arrays=${primitiveArrays.pretty()}, object arrays=${objectArrays.pretty()})"

    override fun run(callback: Task.Callback) {
        passed = 0

        callback.saveProgress(this, true)

        val instanceInsert = InsertCollector("instances") { list ->
            transactionOperations.executeWithoutResult { heapScope.instances.persistAll(list) }
        }
        val fieldValuesInsert = InsertCollector("field values") { list ->
            transactionOperations.executeWithoutResult { heapScope.fieldValues.persistAll(list) }
        }
        val primitiveArrayInsert = InsertCollector("primitive array items") { list ->
            transactionOperations.executeWithoutResult { heapScope.primitiveArrayItems.persistAll(list) }
        }
        val objectArrayInsert = InsertCollector("object array items") { list ->
            transactionOperations.executeWithoutResult { heapScope.objectArrayItems.persistAll(list) }
        }

        val task = this
        arrayOf(instanceInsert, fieldValuesInsert, primitiveArrayInsert, objectArrayInsert).use {
            hprof.scanDumps(object : DumpVisitor {
                override fun onInstanceDump(view: InstanceDumpView) {
                    persistInstance(view, instanceInsert, fieldValuesInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onPrimitiveArrayDump(view: PrimitiveArrayDumpView) {
                    persistPrimitiveArray(view, instanceInsert, primitiveArrayInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onObjectArrayDump(view: ObjectArrayDumpView) {
                    persistObjectArray(view, instanceInsert, objectArrayInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onRootUnknown(view: RootUnknownDumpView) = javaRootCollector.onRootUnknown(view.objectId)
                override fun onRootJniGlobal(view: RootJniGlobalDumpView) = javaRootCollector.onRootJniGlobal(view.objectId)
                override fun onRootJniLocal(view: RootJniLocalDumpView) = javaRootCollector.onRootJniLocal(view.objectId)
                override fun onRootThreadObject(view: RootThreadObjectDumpView) = javaRootCollector.onRootThreadObject(view.objectId)
                override fun onRootJavaFrame(view: RootJavaFrameDumpView) = javaRootCollector.onRootJavaFrame(view.objectId)
                override fun onRootStickyClass(view: RootStickyClassDumpView) = javaRootCollector.onRootStickyClass(view.objectId)
                override fun onRootNativeStack(view: RootNativeStackDumpView) = javaRootCollector.onRootNativeStack(view.objectId)
                override fun onRootThreadBlock(view: RootThreadBlockDumpView) = javaRootCollector.onRootThreadBlock(view.objectId)
                override fun onRootMonitorUsed(view: RootMonitorUsedDumpView) = javaRootCollector.onRootMonitorUsed(view.objectId)
            })
        }

        callback.saveProgress(this, true)
    }

    private fun persistObjectArray(dump: ObjectArrayDumpView, instanceInsert: (InstanceEntity) -> Unit, arrayInsert: (ObjectArrayEntity) -> Unit) {
        objectArrays++
        val instanceNumber = classCountCollector.increment(dump.classId)
        instanceInsert(
            InstanceEntity(
                dump.objectId.toLong(), instanceNumber, dump.classId.toLong(), 0,
                dump.arraySize * HprofValueType.Object.size.toLong(), HprofValueType.Object.tag.toByte(), dump.arraySize,
                null, null
            )
        )
        dump.items
            .mapIndexed { index, item -> ObjectArrayEntity(dump.objectId.toLong(), index, item.toLong()) }
            .filter { it.itemInstanceId > 0 }
            .forEach(arrayInsert)
    }

    private fun persistPrimitiveArray(view: PrimitiveArrayDumpView, instanceInsert: (InstanceEntity) -> Unit, arrayInsert: (PrimitiveArrayEntity) -> Unit) {
        primitiveArrays++
        val classObjectId = classDumpLookup.lookupPrimitiveArrayClassId(view.arrayItemType)
        val instanceNumber = classCountCollector.increment(classObjectId)
        instanceInsert(
            InstanceEntity(
                view.arrayObjectId.toLong(), instanceNumber, classObjectId.toLong(), 0,
                view.arrayItemType.size.toLong() * view.arrayItemCount.toLong(), view.arrayItemType.tag.toByte(), view.arrayItemCount,
                null, null
            )
        )
        if (loadPrimitiveArrayItems) {
            view.arrayItems()
                .mapIndexed { index, item -> PrimitiveArrayEntity(view.arrayObjectId.toLong(), index, item.toString()) }
                .forEach(arrayInsert)
        }
    }

    private fun persistInstance(
        dump: InstanceDumpView,
        instanceEntitySaver: (entity: InstanceEntity) -> Unit,
        fieldEntitySaver: (entity: FieldValueEntity) -> Unit
    ) {
        instancesLoaded++
        val classDump = classDumpLookup.lookup(dump.classObjectId)
        val instanceNumber = classCountCollector.increment(dump.classObjectId)

        val instanceEntity = InstanceEntity(dump.objectId.toLong(), instanceNumber, dump.classObjectId.toLong(), 0, classDump.instanceSize.toLong())
        instanceEntitySaver(instanceEntity)

        if (dump.hasFieldData()) {
            val fieldReader = dump.getFieldReader()
            var fieldDefiner: ClassDump? = classDump
            while (fieldReader.available() && fieldDefiner != null) {
                val classFields = fieldEntityLookup.getInstanceFieldList(fieldDefiner.classObjectId)
                for (field in classFields) {
                    val fieldType = field.type
                    val (valueText, valueInstance) = if (fieldType == HprofValueType.Object) {
                        val instanceId = fieldReader.ulong()
                        instanceId.toString() to instanceId
                    } else {
                        fieldReader.primitiveText(fieldType) to 0UL
                    }

                    val fieldValueEntity = FieldValueEntity(dump.objectId.toLong(), field.id, valueText, valueInstance.toLong())
                    fieldEntitySaver(fieldValueEntity)
                }
                fieldDefiner = if (fieldDefiner.superClassObjectId == 0UL) null else classDumpLookup.lookup(fieldDefiner.superClassObjectId)
            }

            assert(!fieldReader.available())
        }
    }
}