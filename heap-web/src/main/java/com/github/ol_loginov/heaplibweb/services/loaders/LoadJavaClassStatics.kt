package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.springframework.transaction.support.TransactionOperations

internal class LoadJavaClassStatics(
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classDumpLookup: ClassDumpLookup,
    private val fieldEntityLookup: FieldEntityLookup
) : Task {
    private var classCount: Int = 0
    private var passed: Int = 0
    private var fieldsLoaded: Int = 0

    override fun getText(): String = "import class statics: $passed/$classCount classes (fields=${fieldsLoaded})"

    override fun run(callback: Task.Callback) {
        val task = this
        classCount = classDumpLookup.classCount
        passed = 0

        val fieldValuesInsert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.fieldValues.persistAll(list) }
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

        val fields = fieldEntityLookup.getStaticFieldList(dump.classObjectId).associateBy { it.name }
        dump.staticFields
            .filter { it.name.name != null }
            .forEach { value ->
                fields[value.name.name]?.let { field ->
                    val fieldType = field.type
                    val (valueText, valueInstance) = if (fieldType == HprofValueType.Object) {
                        val instanceId = (value.value as ULong).toLong()
                        instanceId.toString() to instanceId
                    } else {
                        value.value.toString() to 0L
                    }

                    val fieldValueEntity = FieldValueEntity(dump.classObjectId.toLong(), field.id, true, valueText, valueInstance)
                    insert(fieldValueEntity)
                    fieldsLoaded++
                }
            }
    }
}