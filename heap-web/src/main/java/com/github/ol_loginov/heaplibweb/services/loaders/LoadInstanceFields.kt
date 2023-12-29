package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.ObjectFieldValue
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadInstanceFields(
    private val heap: Heap,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val typeIdLookup: TypeIdLookup
) : Task {
    private val classesPassed = AtomicLong()
    private val fieldsLoaded = AtomicLong()
    @Volatile
    private var total: Long = 0

    override fun getText(): String = "import instance fields: $classesPassed/$total classes (fields=${fieldsLoaded.get()})"

    override fun run(callback: Task.Callback) {
        val allClasses = heap.allClasses
        total = allClasses.size.toLong()
        classesPassed.set(0)

        callback.saveProgress(this, true)

        val classFieldLookup = ClassFieldLookup(heapScope, transactionOperations, typeIdLookup)
        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult {
                heapScope.fieldValues.persistAll(list)
            }
        }

        insert.use {
            allClasses.forEach { clazz: JavaClass ->
                persistFieldValues(clazz, classFieldLookup, insert)
                classesPassed.incrementAndGet()
                callback.saveProgress(this, false)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistFieldValues(clazz: JavaClass, classFieldLookup: ClassFieldLookup, collector: Consumer<FieldValueEntity>) {
        for (fieldValue in clazz.staticFieldValues) {
            fieldsLoaded.incrementAndGet()
            val fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.field)
            val fieldValueInstance = FieldValueEntity(clazz.javaClassId, fieldValue.definingInstance.instanceId, fieldEntityId, true, fieldValue.value)
            if (fieldValue is ObjectFieldValue) {
                fieldValueInstance.valueInstanceId = fieldValue.instanceId
            }
            collector.accept(fieldValueInstance)
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
                collector.accept(fieldValueInstance)
            }
        }
    }
}