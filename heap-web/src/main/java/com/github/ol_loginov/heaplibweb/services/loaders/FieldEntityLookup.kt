package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories

internal class FieldEntityLookup {
    private val fields = mutableMapOf<ULong, MutableList<FieldEntity>>()

    fun refresh(heapRepositories: HeapRepositories) {
        heapRepositories.fields.streamAllOrderById().use { stream ->
            stream.forEach {
                fields
                    .computeIfAbsent(it.declaringClassId.toULong()) { mutableListOf() }
                    .add(it)
            }
        }
    }

    fun getInstanceFieldList(classObjectId: ULong): List<FieldEntity> {
        val list = fields[classObjectId] ?: return emptyList()
        return list.filter { !it.staticFlag }
    }

    fun getStaticFieldList(classObjectId: ULong): List<FieldEntity> {
        val list = fields[classObjectId] ?: return emptyList()
        return list.filter { it.staticFlag }
    }
}