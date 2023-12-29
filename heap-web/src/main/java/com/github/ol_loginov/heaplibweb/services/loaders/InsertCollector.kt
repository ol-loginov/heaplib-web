package com.github.ol_loginov.heaplibweb.services.loaders

import java.util.function.Consumer

internal class InsertCollector<T>(
    private val batchSize: Int,
    private val finalizer: (List<T>) -> Unit
) : Consumer<T>, AutoCloseable {
    private var queue = mutableListOf<T>()

    override fun accept(item: T) {
        queue.add(item)
        complete(false)
    }

    fun complete(force: Boolean = true) {
        if (!force && queue.size < batchSize) {
            return
        }
        finalizer(queue)
        queue = mutableListOf()
    }

    override fun close() {
        complete()
    }
}