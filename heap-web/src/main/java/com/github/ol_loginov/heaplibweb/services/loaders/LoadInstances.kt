package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.DumpVisitor
import com.github.ol_loginov.heaplibweb.hprof.HprofFile
import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.hprof.RootJavaFrameDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootJniGlobalDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootJniLocalDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootMonitorUsedDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootNativeStackDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootStickyClassDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootThreadBlockDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootThreadObjectDumpView
import com.github.ol_loginov.heaplibweb.hprof.RootUnknownDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.InstanceDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.ObjectArrayDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.PrimitiveArrayDumpView
import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import com.github.ol_loginov.heaplibweb.repository.heap.ObjectArrayEntity
import com.github.ol_loginov.heaplibweb.repository.heap.PrimitiveArrayEntity
import com.github.ol_loginov.heaplibweb.support.pretty
import com.github.ol_loginov.heaplibweb.support.use
import org.springframework.transaction.support.TransactionOperations

internal class LoadInstances(
    private val hprof: HprofFile,
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
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
    private var fieldsLoaded = 0
    private var arrayItemsLoaded = 0

    override fun getText() = "import instances: ${passed.pretty()} dumps (instances=${instancesLoaded.pretty()}, primitive arrays=${primitiveArrays.pretty()}, object arrays=${objectArrays.pretty()} with ${arrayItemsLoaded.pretty()} items, field values=${fieldsLoaded.pretty()})"

    override fun run(callback: Task.Callback) {
        passed = 0

        callback.saveProgress(this, true)

        val instanceInsert = InsertCollector("instances") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.instanceLoader.persistAll(list) }
        }
        val fieldValuesInsert = InsertCollector("field values") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.fieldValueLoader.persistAll(list) }
        }
        val primitiveArrayInsert = InsertCollector("primitive array items") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.primitiveArrayItems.persistAll(list) }
        }
        val objectArrayInsert = InsertCollector("object array items") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.objectArrayLoader.persistAll(list) }
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
        val classDump = classDumpLookup.lookup(dump.classId)
        val instanceNumber = classCountCollector.increment(dump.classId)
        instanceInsert(
            InstanceEntity(
                dump.objectId.toLong(), dump.fo, instanceNumber, dump.classId.toLong(), 0,
                classDump.instanceSize, HprofValueType.Object.tag.toByte(), dump.arraySize
            )
        )
        dump.items
            .mapIndexed { index, item -> ObjectArrayEntity(dump.objectId.toLong(), index, item.toLong()) }
            .filter { it.itemInstanceId > 0 }
            .forEach(arrayInsert)
        arrayItemsLoaded += dump.arraySize
    }

    private fun persistPrimitiveArray(view: PrimitiveArrayDumpView, instanceInsert: (InstanceEntity) -> Unit, arrayInsert: (PrimitiveArrayEntity) -> Unit) {
        primitiveArrays++
        val classObjectId = classDumpLookup.lookupPrimitiveArrayClassId(view.arrayItemType)
        val instanceNumber = classCountCollector.increment(classObjectId)
        instanceInsert(
            InstanceEntity(
                view.arrayObjectId.toLong(), view.fo, instanceNumber, classObjectId.toLong(), 0,
                0, view.arrayItemType.tag.toByte(), view.arrayItemCount,
                view.arrayItemType.size.toLong() * view.arrayItemCount.toLong(), null
            )
        )
        if (loadPrimitiveArrayItems) {
            view.arrayItems()
                .mapIndexed { index, item -> PrimitiveArrayEntity(view.arrayObjectId.toLong(), index, item.toString()) }
                .forEach(arrayInsert)
            arrayItemsLoaded += view.arrayItemCount
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

        val instanceEntity = InstanceEntity(dump.objectId.toLong(), dump.fo, instanceNumber, dump.classObjectId.toLong(), 0, classDump.instanceSize.toInt())
        instanceEntitySaver(instanceEntity)

        if (dump.hasFieldData()) {
            val fieldReader = dump.getFieldReader()
            var fieldDefiner: ClassDump? = classDump
            while (fieldReader.available() && fieldDefiner != null) {
                val classFields = fieldEntityLookup.getInstanceFieldList(fieldDefiner.classObjectId)
                for (field in classFields) {
                    val fieldType = field.type
                    val valueAny = fieldReader.any(fieldType)

                    if (fieldType == HprofValueType.Object) {
                        val fieldValueEntity = FieldValueEntity(dump.objectId.toLong(), field.id, (valueAny as ULong).toLong())
                        fieldsLoaded++
                        fieldEntitySaver(fieldValueEntity)
                    }
                }
                fieldDefiner = if (fieldDefiner.superClassObjectId == 0UL) null else classDumpLookup.lookup(fieldDefiner.superClassObjectId)
            }

            assert(!fieldReader.available())
        }
    }
}