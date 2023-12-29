package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus
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
import kotlin.math.roundToInt

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class InputLoader @Inject constructor(
    private val transactionOperations: TransactionOperations,
    private val heapFileRepository: HeapFileRepository,
    private val inputFilesManager: InputFilesManager
) : Runnable, Task.Callback {
    companion object {
        const val DEFAULT_BUFFER_MB = 100L

        private val log = LoggerFactory.getLogger(InputLoader::class.java)
    }

    private var heapFileId = 0
    var heapFile: HeapFile? = null

    private var progressLimit: Long = 1
    private var progressCurrent: Long = 0

    @Volatile
    private var progressSaved = Instant.now()

    fun withFile(heapFileId: Int): InputLoader {
        this.heapFileId = heapFileId
        return this
    }

    private fun loadFileEntity(): HeapFile {
        this.heapFile = transactionOperations.execute {
            heapFileRepository.findById(heapFileId)?.also { heapFile ->
                heapFile.status = HeapFileStatus.LOADING
                heapFileRepository.merge(heapFile)

            }
        } ?: throw IllegalStateException("no HeapFile#${heapFileId}")
        return this.heapFile!!
    }

    override fun saveProgress(loadMessage: String, force: Boolean) {
        if (!force && Duration.between(progressSaved, Instant.now()).toMillis() < 1000) {
            return
        }
        val progress = if (progressLimit > 0) (1000 * (progressCurrent / progressLimit.toDouble())).roundToInt() else 0
        transactionOperations.executeWithoutResult {
            heapFileRepository.findById(heapFileId)?.also {
                it.loadProgress = progress / 1000f
                it.loadMessage = loadMessage
                heapFileRepository.merge(it)
            } ?: throw IllegalStateException("no HeapFile#${heapFileId}")
        }
        log.info("progress '{}': {}", loadMessage, progress / 10.0)
        progressSaved = Instant.now()
    }

    override fun run() {
        try {
            runUnsafe()
        } catch (e: Throwable) {
            log.error("load failure: {}", e.message, e)

            heapFile?.let { heapFile ->
                transactionOperations.execute {
                    heapFile.status = HeapFileStatus.LOADING_ERROR
                    heapFile.loadError = e.message
                    heapFileRepository.merge(heapFile)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun runUnsafe() {
        saveProgress("create start entities", true)
        val heapFile = loadFileEntity()
        val dump = inputFilesManager.resolveInputFilePath(heapFile.path).toFile().absoluteFile

        saveProgress("open hprof file: ${dump.absolutePath}", true)
        val heap = if (HeapFactory2.canBeMemMapped(dump)) {
            HeapFactory2.createFastHeap(dump)
        } else {
            HeapFactory2.createFastHeap(dump, DEFAULT_BUFFER_MB * 1024 * 1024)
        }

        saveProgress("create scope tables", true)
        heapFile.generateTablePrefix()
        transactionOperations.execute { heapFileRepository.merge(heapFile) }

        val heapScope = heapFileRepository.getScope(heapFile)
        heapScope.createTables()

        saveProgress("read hprof summary", true)
        val summary = heap.summary

        saveProgress("read hprof all classes", true)
        val allClasses = heap.allClasses

        progressLimit = allClasses.size * 3L + summary.totalAllocatedInstances
        progressCurrent = 0

        val typeIdLookup = TypeIdLookup(heapScope)
        val stepTimings = mutableListOf<StepTimings>()
        val stepList = listOf(
            LoadJavaClasses(heap, transactionOperations, heapScope),
            LoadJavaClassFields(heap, transactionOperations, heapScope, typeIdLookup),
            LoadInstances(heap, transactionOperations, heapScope),
            LoadInstanceFields(heap, transactionOperations, heapScope, typeIdLookup)
        )

        stepList.forEach {
            stepTimings.add(runStep(it))
        }

        saveProgress("HeapFile#${heapFile.id} - complete loading", true)

        val stepTimingsTotal = stepTimings.sumOf { it.duration.toMillis() }
        val stepTimingsTextLength = stepTimings.maxOf { it.description.length }
        val stepTimingsText = stepTimings
            .mapIndexed { index, it -> "${index + 1}) ${it.description.padEnd(stepTimingsTextLength)}\t${it.duration.toMillis() / 1000.0} sec" }
            .joinToString("\n")
        saveProgress("Step timings for HeapFile#${heapFile.id}: total time - ${stepTimingsTotal / 1000.0} sec\n$stepTimingsText", true)
    }

    private fun runStep(task: Task): StepTimings {
        log.info("execute ${task.javaClass.simpleName}")
        val start = System.currentTimeMillis()
        task.run(this)
        val end = System.currentTimeMillis()
        return StepTimings(task.getText(), Duration.ofMillis(end - start))
    }

    private data class StepTimings(val description: String, val duration: Duration)
}