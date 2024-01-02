package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope

internal class FieldEntityLookup(
    private val heapScope: HeapScope
) {
    private val fields = mutableMapOf<ULong, List<FieldEntity>>()

    fun getInstanceFieldList(classObjectId: ULong): List<FieldEntity> {
        return fields
            .computeIfAbsent(classObjectId) { heapScope.fields.findAllByDeclaringClassIdOrderById(classObjectId.toLong()) }
            .filter { !it.staticFlag }
    }

    fun getStaticFieldList(classObjectId: ULong): List<FieldEntity> {
        return fields
            .computeIfAbsent(classObjectId) { heapScope.fields.findAllByDeclaringClassIdOrderById(classObjectId.toLong()) }
            .filter { it.staticFlag }
    }
}