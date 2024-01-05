package com.github.ol_loginov.heaplibweb.services.loaders

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(InsertCollector::class.java)

internal class InsertCollector<T>(
    private val entityDescription: String,
    private val batchSize: Int,
    private val finalizer: (List<T>) -> Unit
) : (T) -> Unit, AutoCloseable {
    constructor(entityDescription: String, finalizer: (List<T>) -> Unit) : this(entityDescription, 50000, finalizer)

    private var queue = mutableListOf<T>()

    override fun invoke(item: T) {
        queue.add(item)
        complete(false)
    }

    private fun complete(force: Boolean = true) {
        if (queue.isEmpty()) return
        if (!force && queue.size < batchSize) return

        log.debug("send {} {}", queue.size, entityDescription)
        finalizer(queue)
        queue = mutableListOf()
    }

    override fun close() {
        complete()
    }
}