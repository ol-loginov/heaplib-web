package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong

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
    private var total: Long = 0

    override fun getText(): String = "import java classes: $passed/$total";

    override fun run(callback: Task.Callback) {
        val all = heap.allClasses
        total = all.size.toLong()
        passed.set(0)

        callback.saveProgress(this, true)
        heap.allClasses.forEach { clazz: JavaClass ->
            transactionOperations.executeWithoutResult { _ ->
                persistJavaClass(clazz)
                passed.incrementAndGet()
                callback.saveProgress(this)
            }
        }
    }

    private fun persistJavaClass(clazz: JavaClass) {
        log.info("{}", clazz.name)
        val clazzEntity = JavaClassEntity(
            clazz.javaClassId,
            clazz.name,
            clazz.allInstancesSize,
            clazz.isArray,
            clazz.instanceSize,
            clazz.instancesCount,
            HeapScope.notReadyValueOnError { clazz.retainedSizeByClass },
            clazz.superClass?.javaClassId
        )
        scope.javaClasses.persist(clazzEntity)
    }
}