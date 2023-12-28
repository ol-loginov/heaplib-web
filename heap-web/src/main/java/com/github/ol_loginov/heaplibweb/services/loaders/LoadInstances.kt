package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.ObjectFieldValue
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadInstances(
    private val heap: Heap,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val typeIdLookup: TypeIdLookup
) : Task {
    private val passed = AtomicLong()
    @Volatile
    private var total: Long = 0

    private val instancesLoaded = AtomicLong()
    private val fieldsLoaded = AtomicLong()

    override fun getText() = "import instances: " + passed + "/" + total + " (instances=" + instancesLoaded.get() + ", fields=" + fieldsLoaded.get() + ")"

    override fun run(callback: Task.Callback) {
        val classFieldLookup = ClassFieldLookup(heapScope, typeIdLookup)

        val allClasses = heap.allClasses
        total = allClasses.size.toLong()
        passed.set(0)

        callback.saveProgress(this, true)

        val instanceBatchInsert = InstanceBatchInsert(heapScope)
        allClasses.forEach { clazz: JavaClass ->
            transactionOperations.executeWithoutResult {
                persistInstances(clazz, instanceBatchInsert)
                passed.incrementAndGet()
                callback.saveProgress(this, false)
            }
        }

        transactionOperations.executeWithoutResult { instanceBatchInsert.batchInsert(true) }

        passed.set(0)
        callback.saveProgress(this, true)

        allClasses.forEach { clazz: JavaClass ->
            transactionOperations.executeWithoutResult {
                persistFieldValues(clazz, classFieldLookup)
                passed.incrementAndGet()
                callback.saveProgress(this, false)
            }
        }

        callback.saveProgress(this, true)
    }

    private class InstanceBatchInsert(
        private val heapScope: HeapScope
    ) : Consumer<InstanceEntity> {
        var instanceList: MutableList<InstanceEntity> = ArrayList()

        override fun accept(instanceEntity: InstanceEntity) {
            instanceList.add(instanceEntity)
            batchInsert(false)
        }

        fun batchInsert(force: Boolean) {
            if (!force && instanceList.size < 1000) {
                return
            }
            heapScope.instances.persistAll(instanceList)
            instanceList = ArrayList()
        }
    }

    private fun persistInstances(clazz: JavaClass, instanceEntityConsumer: Consumer<InstanceEntity>) {
        val instanceNumber = AtomicInteger()
        for (instance in clazz.instances) {
            instancesLoaded.incrementAndGet()
            instanceEntityConsumer.accept(
                InstanceEntity(
                    instance.instanceId, instanceNumber.incrementAndGet(), clazz.javaClassId,
                    instance.isGCRoot, instance.size, null, null
                )
            )
        }
    }

    private fun persistFieldValues(clazz: JavaClass, classFieldLookup: ClassFieldLookup) {
        val fieldValues = ArrayList<FieldValueEntity>()
        for (fieldValue in clazz.staticFieldValues) {
            fieldsLoaded.incrementAndGet()
            val fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.field)
            val fieldValueInstance = FieldValueEntity(clazz.javaClassId, fieldValue.definingInstance.instanceId, fieldEntityId, true, fieldValue.value)
            if (fieldValue is ObjectFieldValue) {
                fieldValueInstance.valueInstanceId = fieldValue.instanceId
            }
            fieldValues.add(fieldValueInstance)
        }
        for (instance in clazz.instances) {
            for (fieldValue in instance.fieldValues) {
                fieldsLoaded.incrementAndGet()
                val fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.field)
                assert(fieldValue.definingInstance.instanceId == instance.instanceId)
                val fieldValueInstance = FieldValueEntity(clazz.javaClassId, fieldValue.definingInstance.instanceId, fieldEntityId, false, fieldValue.value)
                if (fieldValue is ObjectFieldValue) {
                    fieldValueInstance.valueInstanceId = fieldValue.instanceId
                }
                fieldValues.add(fieldValueInstance)
            }
        }
        heapScope.fieldValues.persistAll(fieldValues)
    }
}