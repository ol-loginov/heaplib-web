package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepository
import com.github.ol_loginov.heaplibweb.services.InputFilesManager
import jakarta.inject.Inject
import org.netbeans.lib.profiler.heap.HeapFactory2
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionOperations
import java.io.IOException
import java.time.Duration
import java.time.Instant

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class InputLoader @Inject constructor(
    private val transactionOperations: TransactionOperations,
    private val heapFileRepository: HeapFileRepository,
    private val heapRepository: HeapRepository,
    private val inputLoadWorkFactory: InputFilesManager
) : Runnable, Task.Callback {
    companion object {
        const val DEFAULT_BUFFER_MB = 100L

        private val log = LoggerFactory.getLogger(InputLoader::class.java)
    }

    private var entityId = 0

    private var progressLimit: Long = 1
    private var progressCurrent: Long = 0
    @Volatile
    private var progressSaved = Instant.now()

    fun withEntityId(entityId: Int): InputLoader {
        this.entityId = entityId
        return this
    }

    private fun loadFileEntity(): HeapFile? {
        return transactionOperations.execute { _ ->
            heapFileRepository.findById(entityId)?.also {
                it.status = HeapFileStatus.LOADING
                heapFileRepository.merge(it)
            } ?: throw IllegalStateException("no HeapFile#${entityId}")
        }
    }

    override fun saveProgress(task: Task, force: Boolean) {
        if (!force && Duration.between(progressSaved, Instant.now()).toMillis() < 1000) {
            return
        }
        val progress = if (progressLimit > 0) Math.round(1000 * (progressCurrent / progressLimit.toDouble())) else 0
        transactionOperations.executeWithoutResult {
            heapFileRepository.findById(entityId)?.also {
                it.loadProgress = progress / 1000f
                it.loadMessage = task.getText()
                heapFileRepository.merge(it)
            } ?: throw IllegalStateException("no HeapFile#${entityId}")
        }
        InputLoader.log.info("progress '{}': {}", task.getText(), progress / 10.0)
        progressSaved = Instant.now()
    }

    override fun run() {
        try {
            runUnsafe()
        } catch (e: Throwable) {
            InputLoader.log.error("load failure: {}", e.message, e)
        }
    }


    @Throws(IOException::class)
    private fun runUnsafe() {
        val entity = loadFileEntity()
        val dump = inputLoadWorkFactory.getInputFile(entity!!.relativePath).toFile()
        val heap = if (HeapFactory2.canBeMemMapped(dump)) HeapFactory2.createFastHeap(dump) else HeapFactory2.createFastHeap(dump, DEFAULT_BUFFER_MB * 1024 * 1024)
        val heapEntity = heapRepository.persist(HeapEntity(entity))

        val summary = heap.summary
        val allClasses = heap.allClasses
        progressLimit = allClasses.size * 3L + summary.totalAllocatedInstances
        progressCurrent = 0

        val heapScope = heapRepository.createScope(heapEntity)
        val typeIdLookup = TypeIdLookup(heapScope)
        runStep(LoadJavaClasses(heap, transactionOperations, heapScope))
        runStep(LoadJavaClassFields(heap, transactionOperations, heapScope, typeIdLookup))
        runStep(LoadInstances(heap, transactionOperations, heapScope, typeIdLookup))
    }

    private fun runStep(task: Task) {
        task.run(this)
    }
}