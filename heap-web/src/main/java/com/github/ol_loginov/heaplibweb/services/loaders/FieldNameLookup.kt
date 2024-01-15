package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.StringRef
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories

class FieldNameLookup {
    private val nameToId = mutableMapOf<String, Int>()

    fun refresh(heapRepositories: HeapRepositories) {
        heapRepositories.names.streamAll().use { stream ->
            stream.forEach {
                nameToId[it.name] = it.id
            }
        }
    }

    fun nameToId(name: String) = nameToId[name] ?: throw IllegalArgumentException("name '${name}' is not registered")
    fun nameToId(name: StringRef) = nameToId[name.orEmpty()] ?: throw IllegalArgumentException("name @${name.id} is not registered")
}