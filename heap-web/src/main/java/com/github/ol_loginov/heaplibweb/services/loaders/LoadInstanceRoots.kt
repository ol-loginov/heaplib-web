package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.support.pretty
import org.springframework.transaction.support.TransactionOperations

class LoadInstanceRoots(
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope,
    private val javaRootCollector: JavaRootCollector
) : Task {
    private var total: Int = 0
    private var passed: Int = 0

    override fun getText(): String = "update instance roots: ${passed.pretty()}/${total.pretty()}"

    override fun run(callback: Task.Callback) {
        val insert = InsertCollector("instance roots") { list ->
            transactionOperations.executeWithoutResult { heapScope.instances.updateRoots(list) }
        }

        val task = this
        callback.saveProgress(task, true)

        insert.use {
            javaRootCollector.objectRootFlags.forEach { classObjectId, number ->
                insert(classObjectId to number)
                callback.saveProgress(task, false)
            }
        }

        callback.saveProgress(task, true)
    }
}