package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.support.pretty
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations
import java.util.function.Consumer

private val log = LoggerFactory.getLogger(LoadJavaClassFields::class.java)

internal class LoadJavaClassFields(
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classDumpLookup: ClassDumpLookup,
    private val fieldEntityLookup: FieldEntityLookup,
    private val fieldNameLookup: FieldNameLookup
) : Task {
    private var passed = 0
    private var classCount = 0
    private var fieldsLoaded = 0

    override fun getText(): String = "import class fields: ${passed.pretty()}/${classCount.pretty()} classes (fields=${fieldsLoaded.pretty()})"

    override fun run(callback: Task.Callback) {
        classCount = classDumpLookup.classCount
        passed = 0

        callback.saveProgress(this, true)

        val insert = InsertCollector("fields") { list ->
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

        log.info("refresh fields set")
        transactionOperations.executeWithoutResult { _ ->
            fieldEntityLookup.refresh(heapScope)
        }

        callback.saveProgress(this, true)
    }

    private fun persistJavaClassFields(clazz: ClassDump, consumer: Consumer<FieldEntity>) {
        for (field in clazz.instanceFields) {
            fieldsLoaded++
            consumer.accept(
                FieldEntity(clazz.classObjectId.toLong(), fieldNameLookup.nameToId(field.name), false, field.type.tag.toByte())
            )
        }

        for (field in clazz.staticFields) {
            fieldsLoaded++
            consumer.accept(
                FieldEntity(clazz.classObjectId.toLong(), fieldNameLookup.nameToId(field.name), true, field.type.tag.toByte())
            )
        }
    }
}
