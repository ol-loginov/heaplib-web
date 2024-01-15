package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.support.pretty
import org.springframework.transaction.support.TransactionOperations

internal class LoadJavaClassStatics(
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
    private val classDumpLookup: ClassDumpLookup,
    private val fieldEntityLookup: FieldEntityLookup,
    private val fieldNameLookup: FieldNameLookup
) : Task {
    private var classCount: Int = 0
    private var passed: Int = 0
    private var fieldsLoaded: Int = 0

    override fun getText(): String = "import class statics: ${passed.pretty()}/${classCount.pretty()} classes (fields=${fieldsLoaded.pretty()})"

    override fun run(callback: Task.Callback) {
        val task = this
        classCount = classDumpLookup.classCount
        passed = 0

        val fieldValuesInsert = InsertCollector("field values") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.fieldValueLoader.persistAll(list) }
        }

        fieldValuesInsert.use {
            classDumpLookup.classes.forEach { dump ->
                insertStaticFields(dump, fieldValuesInsert)
                passed++
                callback.saveProgress(task, false)
            }
        }
    }

    private fun insertStaticFields(dump: ClassDump, insert: (FieldValueEntity) -> Unit) {
        if (dump.staticFields.isEmpty()) return

        val fields = fieldEntityLookup
            .getStaticFieldList(dump.classObjectId)
            .associateBy { it.nameId }
        dump.staticFields
            .filter { it.name.name != null }
            .forEach { value ->
                fields[fieldNameLookup.nameToId(value.name)]?.let { field ->
                    val fieldValueReference = if (value.type == HprofValueType.Object) (value.value as ULong).toLong() else 0L
                    val fieldValueEntity = FieldValueEntity(dump.classObjectId.toLong(), field.id, fieldValueReference)
                    if (value.type == HprofValueType.Object) {
                        insert(fieldValueEntity)
                        fieldsLoaded++
                    }
                }
            }
    }
}