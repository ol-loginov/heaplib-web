package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.TypeEntity

class TypeIdLookup(
    private val heapScope: HeapScope
) {
    private val cache = HashMap<String, Int>()

    fun lookupTypeId(name: String): Int {
        return cache.computeIfAbsent(name) { n -> registerType(n) }
    }

    private fun registerType(name: String): Int {
        return heapScope.types
            .persist(TypeEntity(name))
            .id
    }
}