package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.support.pretty
import org.springframework.transaction.support.TransactionOperations

class LoadInstanceRootFlags(
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
    private val javaRootCollector: JavaRootCollector
) : Task {
    private var total: Int = 0
    private var passed: Int = 0

    override fun getText(): String = "update instance roots: ${passed.pretty()}/${total.pretty()}"

    override fun run(callback: Task.Callback) {
        passed = 0
        total = javaRootCollector.objectRootFlags.size

        val insert = InsertCollector("instance roots") { list ->
            transactionOperations.executeWithoutResult { heapRepositories.instances.updateRoots(list) }
        }

        val task = this
        callback.saveProgress(task, true)

        insert.use {
            javaRootCollector.objectRootFlags.forEach { classObjectId, number ->
                insert(classObjectId to number)
                passed++
                callback.saveProgress(task, false)
            }
        }

        callback.saveProgress(task, true)
    }
}