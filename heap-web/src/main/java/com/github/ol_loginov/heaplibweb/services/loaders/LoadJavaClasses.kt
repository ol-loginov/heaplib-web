package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadJavaClasses(
    private val heap: Heap,
    private val transactionOperations: TransactionOperations,
    private val scope: HeapScope
) : Task {
    companion object {
        private val log = LoggerFactory.getLogger(LoadJavaClasses::class.java)
    }

    private val passed = AtomicLong()

    @Volatile
    private var total: Long = -1

    override fun getText(): String = "import java classes: $passed/$total classes";

    override fun run(callback: Task.Callback) {
        val all = heap.allClasses
        total = all.size.toLong()
        passed.set(0)

        callback.saveProgress(this, true)
        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult {
                scope.javaClasses.persistAll(list)
            }
        }

        insert.use {
            heap.allClasses.forEach { clazz: JavaClass ->
                persistJavaClass(clazz, insert)
                passed.incrementAndGet()
                callback.saveProgress(this)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistJavaClass(clazz: JavaClass, collector: Consumer<JavaClassEntity>) {
        log.debug("{}", clazz.name)
        collector.accept(
            JavaClassEntity(
                clazz.javaClassId,
                clazz.name,
                null,
                clazz.isArray,
                clazz.instanceSize,
                null,
                null,
                clazz.superClass?.javaClassId
            )
        )
    }
}