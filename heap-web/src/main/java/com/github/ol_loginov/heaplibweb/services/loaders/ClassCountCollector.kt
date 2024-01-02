package com.github.ol_loginov.heaplibweb.services.loaders

class ClassCountCollector {
    val counters = mutableMapOf<ULong, Int>()

    fun increment(classObjectId: ULong): Int {
        return counters.compute(classObjectId) { _, prev -> (prev ?: 0).inc() } ?: 0
    }
}