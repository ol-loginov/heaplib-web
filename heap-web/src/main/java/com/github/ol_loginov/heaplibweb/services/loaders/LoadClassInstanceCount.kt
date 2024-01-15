package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.support.pretty
import org.springframework.transaction.support.TransactionOperations

class LoadClassInstanceCount(
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
    private val classCountCollector: ClassCountCollector
) : Task {
    private var total: Int = 0
    private var passed: Int = 0

    override fun getText(): String = "update instance count: ${passed.pretty()}/${total.pretty()}"

    override fun run(callback: Task.Callback) {
        passed = 0
        total = classCountCollector.counters.size

        val insert = InsertCollector("classes counts") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.classes.updateCounts(list) }
        }

        val task = this
        callback.saveProgress(task, true)

        insert.use {
            classCountCollector.counters.forEach { classObjectId, number ->
                insert(classObjectId to number)
                passed++
                callback.saveProgress(task, false)
            }
        }

        callback.saveProgress(task, true)
    }
}