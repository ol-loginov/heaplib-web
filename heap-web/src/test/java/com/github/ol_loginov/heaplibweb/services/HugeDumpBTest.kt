package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader
import jakarta.inject.Inject
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

@ContextConfiguration(classes = [HugeDumpBTest.TestContext::class])
@Disabled("only manual execution")
class HugeDumpBTest : DatabaseTest() {
    @Import(InputLoader::class)
    class TestContext {
        @Bean
        @Primary
        fun inputLoadWorkFactory(): InputFilesManager = Mockito.mock(InputFilesManager::class.java)
    }

    @Inject
    private lateinit var inputFilesManager: InputFilesManager

    @Inject
    private lateinit var inputLoaderProvider: ObjectProvider<InputLoader>

    @Inject
    private lateinit var environment: Environment

    private fun hugeDumpPath(): String {
        val pathString = environment.get("test.huge-dump")
        if (pathString.isNullOrBlank()) throw IllegalStateException("no property 'test.huge-dump'. it is required")

        val path = Path.of(pathString)
        if (path.notExists() || !path.isRegularFile() || !path.isReadable()) throw FileNotFoundException(pathString)

        return pathString
    }

    @Test
    @Disabled("only manual execution")
    @Rollback(false)
    fun loadHeapAndKeep() {
        val dumpFile = hugeDumpPath();
        TestTool._when(inputFilesManager.resolveInputFilePath(dumpFile)).thenReturn(Path.of(dumpFile))

        val heapFile = heapFileRepository.persist(HeapFile(dumpFile))
        val work = inputLoaderProvider.getObject()
        work.withFile(heapFile.id)
        work.run()
    }
}