package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadJavaClassFields(
    private val heap: Heap,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val typeIdLookup: TypeIdLookup
) : Task {
    private val passed = AtomicLong()
    @Volatile
    private var total: Long = 0
    private val fieldsLoaded = AtomicLong()

    override fun getText(): String = "import class fields: $passed/$total (fields=${fieldsLoaded.get()})"

    override fun run(callback: Task.Callback) {
        val all = heap.allClasses
        total = all.size.toLong()
        passed.set(0)

        callback.saveProgress(this, true)

        val saver = Saver(heapScope)
        all.forEach(Consumer { clazz: JavaClass ->
            transactionOperations.executeWithoutResult { _ ->
                persistJavaClassFields(clazz, typeIdLookup, saver)
                passed.incrementAndGet()
                callback.saveProgress(this, false)
            }
        })
        transactionOperations.executeWithoutResult { _ -> saver.batchInsert(true) }
    }

    private fun persistJavaClassFields(clazz: JavaClass, nameLookup: TypeIdLookup, fieldEntityConsumer: Consumer<FieldEntity>) {
        for (field in clazz.fields) {
            fieldsLoaded.incrementAndGet()
            fieldEntityConsumer.accept(
                FieldEntity(
                    clazz.javaClassId,
                    field.name, field.isStatic,
                    nameLookup.lookupTypeId(field.type.name)
                )
            )
        }
        for (fieldValue in clazz.staticFieldValues) {
            fieldsLoaded.incrementAndGet()
            val field = fieldValue.field

            fieldEntityConsumer.accept(
                FieldEntity(
                    field.declaringClass.javaClassId,
                    field.name, field.isStatic,
                    nameLookup.lookupTypeId(field.type.name)
                )
            )
        }
    }

    private class Saver(
        private val heapScope: HeapScope
    ) : Consumer<FieldEntity> {
        private var batch = ArrayList<FieldEntity>()

        override fun accept(fieldEntity: FieldEntity) {
            batch.add(fieldEntity)
            batchInsert(false)
        }

        fun batchInsert(force: Boolean) {
            if (!force && batch.size < 1000) {
                return
            }
            heapScope.fields.persistAll(batch)
            batch = ArrayList()
        }
    }
}