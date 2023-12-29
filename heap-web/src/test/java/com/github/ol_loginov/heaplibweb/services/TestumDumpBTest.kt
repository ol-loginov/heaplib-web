package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.TestTool._when
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import java.nio.file.Path

@ContextConfiguration(classes = [TestumDumpBTest.TestContext::class])
class TestumDumpBTest : DatabaseTest() {
    @Import(InputLoader::class)
    class TestContext {
        @Bean
        @Primary
        fun inputLoadWorkFactory(): InputFilesManager {
            return Mockito.mock(InputFilesManager::class.java)
        }
    }

    private val inputFileName = "heapdumps/testum-1703615978111.hprof"

    @TempDir
    private lateinit var tempDir: Path

    @Inject
    private lateinit var inputFilesManager: InputFilesManager

    @Inject
    private lateinit var heapFileRepository: HeapFileRepository

    @Inject
    private lateinit var inputLoaderProvider: ObjectProvider<InputLoader>

    @Test
    fun loadAndQuery() {
        val heap = loadHeap0()
        dropScopeAfterTest(heap)
        runOQL0()
    }

    @Test
    fun loadHeap() {
        val heapEntity = loadHeap0()
        dropScopeAfterTest(heapEntity)
    }

    @Test
    @Disabled("only manual execution")
    @Rollback(false)
    fun loadHeapAndKeep() {
        loadHeap0()
    }

    @Test
    @Disabled("only manual execution")
    fun runOQL() {
        runOQL0()
    }

    private fun loadHeap0(): HeapEntity {
        _when(inputFilesManager.resolveInputFilePath(inputFileName)).thenReturn(TestTool.getResourceFile(inputFileName).toPath())

        val heapFile = heapFileRepository.persist(HeapFile(inputFileName))
        val work = inputLoaderProvider.getObject()
        work.withFile(heapFile.id)
        work.run()

        return work.heapEntity!!
    }

    private fun runOQL0() {
        val heapEntity = heapRepository.findByFileLatest(inputFileName) ?: throw ValueNotReadyException("heap not loaded")
        val heapProxy = HeapProxy(heapRepository.getScope(heapEntity))
        val oql = OQLEngineForTest(heapProxy)
        val results = oql.collectQueryAll("select a from testum.ClassA_Derived a")
        assertThat(results).hasSize(1)
    }
}