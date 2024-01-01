package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofStream
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadJavaClasses(
    private val heap: HprofStream,
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
        passed.set(0)

        callback.saveProgress(this, true)
        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult {
                scope.javaClasses.persistAll(list)
            }
        }

        insert.use {
            heap.scanJavaClasses().forEach { loadClass: ClassDump ->
                persistJavaClass(loadClass, insert)
                passed.incrementAndGet()
                callback.saveProgress(this)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistJavaClass(clazz: ClassDump, collector: Consumer<JavaClassEntity>) {
        if (log.isDebugEnabled) log.debug("{}", clazz.className.name)

        collector.accept(
            JavaClassEntity(
                clazz.classObjectId.toLong(),
                clazz.className.name ?: "",
                null,
                null, // clazz.isArray,
                clazz.instanceSize.toInt(), // clazz.instanceSize,
                null,
                null,
                null //                clazz.superClass?.javaClassId
            )
        )
    }
}