package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.springframework.transaction.support.TransactionOperations
import java.util.function.Consumer

internal class LoadJavaClassFields(
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classDumpLookup: ClassDumpLookup
) : Task {
    private var passed = 0
    private var classCount = 0
    private var fieldsLoaded = 0

    override fun getText(): String = "import class fields: $passed/$classCount classes (fields=${fieldsLoaded})"

    override fun run(callback: Task.Callback) {
        classCount = classDumpLookup.classCount
        passed = 0

        callback.saveProgress(this, true)

        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { _ ->
                heapScope.fields.persistAll(list)
            }
        }

        insert.use {
            classDumpLookup.classes.forEach { classDump ->
                persistJavaClassFields(classDump, insert)
                passed++
                callback.saveProgress(this)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistJavaClassFields(clazz: ClassDump, consumer: Consumer<FieldEntity>) {
        for (field in clazz.instanceFields) {
            fieldsLoaded++
            consumer.accept(
                FieldEntity(clazz.classObjectId.toLong(), field.name.orEmpty(), false, field.type.tag.toByte())
            )
        }

        for (field in clazz.staticFields) {
            fieldsLoaded++
            consumer.accept(
                FieldEntity(clazz.classObjectId.toLong(), field.name.orEmpty(), true, field.type.tag.toByte())
            )
        }
    }
}
