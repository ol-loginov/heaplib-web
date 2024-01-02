package com.github.ol_loginov.heaplibweb.services.loaders

internal class InsertCollector<T>(
    private val batchSize: Int,
    private val finalizer: (List<T>) -> Unit
) : (T) -> Unit, AutoCloseable {
    private var queue = mutableListOf<T>()

    override fun invoke(item: T) {
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