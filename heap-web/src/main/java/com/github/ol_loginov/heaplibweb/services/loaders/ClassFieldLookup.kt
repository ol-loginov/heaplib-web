package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Field
import org.springframework.transaction.support.TransactionOperations
import kotlin.streams.asSequence

internal class ClassFieldLookup(
    private val scope: HeapScope,
    private val transactionOperations: TransactionOperations
) {
    private data class FieldKey(val declaringClassId: Long, val name: String, val isStatic: Boolean)

    private val fieldEntities: MutableMap<FieldKey, Int> by lazy {
        scope.fields.streamAll().use {
            it.asSequence()
                .associateBy({ FieldKey(it.declaringClassId, it.name, it.staticFlag) }, { it.id })
                .toMutableMap()
        }
    }

    fun getFieldEntityId(field: Field): Int {
        val fieldKey = FieldKey(field.declaringClass.javaClassId, field.name, field.isStatic)
        return fieldEntities.computeIfAbsent(fieldKey) { this.createField(field) }
    }

    private fun createField(field: Field): Int {
        TODO("not implemented")
//        return transactionOperations.execute {
//            val fieldEntity = FieldEntity(
//                field.declaringClass.javaClassId, field.name, field.isStatic,
//                typeIdLookup.lookupTypeId(field.type.)
//            )
//            scope.fields.persist(fieldEntity)
//            fieldEntity.id
//        }!!
    }
}