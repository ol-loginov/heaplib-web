package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.JavaClass
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadInstances(
    private val heap: Heap,
    private val transactionOperations: TransactionOperations,
    private val heapScope: HeapScope
) : Task {
    private val passed = AtomicLong()

    @Volatile
    private var total: Long = 0

    private val instancesLoaded = AtomicLong()

    override fun getText() = "import instances: $passed/$total classes (instances=${instancesLoaded.get()})"

    override fun run(callback: Task.Callback) {
        val allClasses = heap.allClasses
        total = allClasses.size.toLong()
        passed.set(0)

        callback.saveProgress(this, true)

        val insert = InsertCollector(1000) { list ->
            transactionOperations.executeWithoutResult {
                heapScope.instances.persistAll(list)
            }
        }

        insert.use {
            allClasses.forEach { clazz: JavaClass ->
                persistInstances(clazz, insert)
                passed.incrementAndGet()
                callback.saveProgress(this, false)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistInstances(clazz: JavaClass, instanceEntityConsumer: Consumer<InstanceEntity>) {
        val instanceNumber = AtomicInteger()
        for (instance in clazz.instances) {
            instancesLoaded.incrementAndGet()
            instanceEntityConsumer.accept(
                InstanceEntity(
                    instance.instanceId, instanceNumber.incrementAndGet(), clazz.javaClassId,
                    instance.isGCRoot, instance.size, null, null
                )
            )
        }
    }
}