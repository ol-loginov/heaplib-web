package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepository
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import java.nio.file.Files
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
    private lateinit var heapRepository: HeapRepository
    @Inject
    private lateinit var heapFileRepository: HeapFileRepository
    @Inject
    private lateinit var inputLoaderProvider: ObjectProvider<InputLoader>

    @Test
    @Rollback(false)
    fun load() {
        val inputFileDump = tempDir.resolve(inputFileName)
        Files.createDirectories(inputFileDump.parent)
        TestTool.copyResourceTo(inputFileName, inputFileDump)
        Mockito.`when`(inputFilesManager.getInputFile(inputFileName)).thenReturn(inputFileDump)

        val heapFile = heapFileRepository.persist(HeapFile(inputFileName))
        val work = inputLoaderProvider.getObject()
        work.withEntityId(heapFile.id)
        work.run()
    }

    @Test
    fun oql() {
        val heapFile = heapFileRepository.findFirstByRelativePathOrderByIdDesc(inputFileName) ?: throw IllegalStateException()
        val heap = heapRepository.findByFile(heapFile) ?: throw IllegalStateException()
        val heapProxy = HeapProxy(heapRepository.createScope(heap))
        val oql = OQLEngineForTest(heapProxy)
        oql.executeQuery("select a from testum.ClassA_Derived a") 
    }
}