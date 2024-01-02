package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.*
import com.github.ol_loginov.heaplibweb.repository.heap.*
import com.github.ol_loginov.heaplibweb.support.use
import org.springframework.transaction.support.TransactionOperations

internal class LoadDumps(
    private val heap: HprofStream,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classDumpLookup: ClassDumpLookup,
    private val classCountCollector: ClassCountCollector,
    private val javaRootCollector: JavaRootCollector,
    private val fieldEntityLookup: FieldEntityLookup,
    private val loadPrimitiveArrayItems: Boolean = false
) : Task {
    private var passed = 0
    private var instancesLoaded = 0
    private var primitiveArrays = 0
    private var objectArrays = 0

    override fun getText() = "import instances: $passed dumps (instances=${instancesLoaded}, primitive arrays=${primitiveArrays}, object arrays=${objectArrays})"

    override fun run(callback: Task.Callback) {
        passed = 0

        callback.saveProgress(this, true)

        val instanceInsert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.instances.persistAll(list) }
        }
        val fieldValuesInsert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.fieldValues.persistAll(list) }
        }
        val primitiveArrayInsert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.primitiveArrayItems.persistAll(list) }
        }
        val objectArrayInsert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.objectArrayItems.persistAll(list) }
        }

        val task = this
        arrayOf(instanceInsert, fieldValuesInsert, primitiveArrayInsert, objectArrayInsert).use {
            heap.scanDumps(object : DumpReceiver {
                override fun onInstance(dump: InstanceDump, fieldReader: InstanceFieldReader) {
                    persistInstance(dump, fieldReader, instanceInsert, fieldValuesInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onPrimitiveArray(dump: PrimitiveArrayDump) {
                    persistPrimitiveArray(dump, instanceInsert, primitiveArrayInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onObjectArray(dump: ObjectArrayDump) {
                    persistObjectArray(dump, instanceInsert, objectArrayInsert)
                    passed++
                    callback.saveProgress(task, false)
                }

                override fun onRootUnknown(objectId: ULong) = javaRootCollector.onRootUnknown(objectId)
                override fun onRootJniGlobal(objectId: ULong, jniGlobalRefId: ULong) = javaRootCollector.onRootJniGlobal(objectId, jniGlobalRefId)
                override fun onRootJniLocal(objectId: ULong, threadSN: UInt, frame: UInt) = javaRootCollector.onRootJniLocal(objectId, threadSN, frame)
                override fun onRootThreadObject(objectId: ULong, threadSN: UInt, stackTraceSN: UInt) = javaRootCollector.onRootThreadObject(objectId, threadSN, stackTraceSN)
                override fun onRootJavaFrame(objectId: ULong, threadSN: UInt, frame: UInt) = javaRootCollector.onRootJavaFrame(objectId, threadSN, frame)
                override fun onRootStickyClass(objectId: ULong) = javaRootCollector.onRootStickyClass(objectId)
                override fun onRootNativeStack(objectId: ULong, threadSN: UInt) = javaRootCollector.onRootNativeStack(objectId, threadSN)
                override fun onRootThreadBlock(objectId: ULong, threadSN: UInt) = javaRootCollector.onRootThreadBlock(objectId, threadSN)
                override fun onRootMonitorUsed(objectId: ULong) = javaRootCollector.onRootMonitorUsed(objectId)
            })
        }

        callback.saveProgress(this, true)
    }

    private fun persistObjectArray(dump: ObjectArrayDump, instanceInsert: (InstanceEntity) -> Unit, arrayInsert: (ObjectArrayEntity) -> Unit) {
        objectArrays++
        val instanceNumber = classCountCollector.increment(dump.arrayClassId)
        instanceInsert(
            InstanceEntity(
                dump.objectId.toLong(), instanceNumber, dump.arrayClassId.toLong(), 0,
                dump.array.size * HprofValueType.Object.size.toLong(), HprofValueType.Object.tag.toByte(), dump.array.size,
                null, null
            )
        )
        dump.array
            .mapIndexed { index, item -> ObjectArrayEntity(dump.objectId.toLong(), index, item.toLong()) }
            .filter { it.itemInstanceId > 0 }
            .forEach(arrayInsert)
    }

    private fun persistPrimitiveArray(dump: PrimitiveArrayDump, instanceInsert: (InstanceEntity) -> Unit, arrayInsert: (PrimitiveArrayEntity) -> Unit) {
        primitiveArrays++
        val classObjectId = classDumpLookup.lookupPrimitiveArrayClassId(dump.type)
        val instanceNumber = classCountCollector.increment(classObjectId)
        instanceInsert(
            InstanceEntity(
                dump.objectId.toLong(), instanceNumber, classObjectId.toLong(), 0,
                dump.array.size * dump.type.size.toLong(), dump.type.tag.toByte(), dump.array.size,
                null, null
            )
        )
        if (loadPrimitiveArrayItems) {
            dump.array
                .mapIndexed { index, item -> PrimitiveArrayEntity(dump.objectId.toLong(), index, item.toString()) }
                .forEach(arrayInsert)
        }
    }

    private fun persistInstance(
        dump: InstanceDump,
        fieldReader: InstanceFieldReader,
        instanceEntitySaver: (entity: InstanceEntity) -> Unit,
        fieldEntitySaver: (entity: FieldValueEntity) -> Unit
    ) {
        instancesLoaded++
        val classDump = classDumpLookup.lookup(dump.classObjectId)
        val instanceNumber = classCountCollector.increment(dump.classObjectId)

        val instanceEntity = InstanceEntity(dump.objectId.toLong(), instanceNumber, dump.classObjectId.toLong(), 0, classDump.instanceSize.toLong())
        instanceEntitySaver(instanceEntity)

        if (dump.hasFieldData()) {
            var fieldDefiner: ClassDump? = classDump
            while (fieldReader.available() && fieldDefiner != null) {
                fieldEntityLookup.getInstanceFieldList(fieldDefiner.classObjectId).forEach { field ->
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