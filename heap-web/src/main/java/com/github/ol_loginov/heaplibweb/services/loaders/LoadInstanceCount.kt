package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.springframework.transaction.support.TransactionOperations

class LoadInstanceCount(
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val classCountCollector: ClassCountCollector
) : Task {
    private var total: Int = 0
    private var passed: Int = 0

    override fun getText(): String = "update instance count: $passed/$total"

    override fun run(callback: Task.Callback) {
        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult { heapScope.javaClasses.updateCounts(list) }
        }

        val task = this
        callback.saveProgress(task, true)

        insert.use {
            classCountCollector.counters.forEach { classObjectId, number ->
                insert(classObjectId to number)
                callback.saveProgress(task, false)
            }
        }

        callback.saveProgress(task, true)
    }
}