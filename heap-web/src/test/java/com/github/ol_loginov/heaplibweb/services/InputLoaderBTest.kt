package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.TestTool._when
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy
import jakarta.inject.Inject
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [InputLoaderBTest.TestContext::class])
@Disabled("manual run")
@Rollback(false)
class InputLoaderBTest : DatabaseTest() {
    @Import(InputLoader::class)
    class TestContext {
        @Bean
        @Primary
        fun inputLoadWorkFactory(): InputFilesManager = Mockito.mock(InputFilesManager::class.java)
    }

    companion object {
        private val log = LoggerFactory.getLogger(InputLoaderBTest::class.java)
    }

    @Inject
    private lateinit var inputFilesManager: InputFilesManager

    @Inject
    private lateinit var inputLoaderProvider: ObjectProvider<InputLoader>

    @Test
    fun load_1703107559883() {
        val inputFileName = "heapdumps/remote-jdbc-1703107559883.sanitized.hprof"
        _when(inputFilesManager.resolveInputFilePath(inputFileName)).thenReturn(TestTool.getResourceFile(inputFileName).toPath())

        val heapFile = heapFileRepository.persist(HeapFile(inputFileName))
        val work = inputLoaderProvider.getObject()
        work.withFile(heapFile.id)
        work.run()
    }

    @Test
    fun runSomeOQL() {
        val printObject = OQLEngine.ObjectVisitor {
            log.info("result: {}", it)
            false
        }

        val heap = heapFileRepository.findAllByOrderByLoadStartDesc().get(0)
        val heapProxy = HeapProxy(heapFileRepository.getHeapRepositories(heap))

        val oql = NetbeansOQLEngineForTest(heapProxy)
        oql.executeQuery("select a from [I a")
        oql.executeQuery("select a from [B a")
        oql.executeQuery("select a from [C a")
        oql.executeQuery("select a from [S a")
        oql.executeQuery("select a from [J a")
        oql.executeQuery("select a from [F a")
        oql.executeQuery("select a from [Z a")
        oql.executeQuery("select a from [java.lang.String a", printObject)
        oql.executeQuery("select a.count from java.lang.String a")
        oql.executeQuery("select map(heap.findClass(\"java.io.File\").fields, 'toHtml(it.name) + \" = \" + toHtml(it.signature)')", printObject)
        oql.executeQuery("select map(a.clazz.statics, 'toHtml(it)') from java.lang.String a", printObject)
        oql.executeQuery("select heap.forEachClass(function(xxx) { print(xxx.name); print(\"\\n\");})", printObject)
        oql.executeQuery("select heap.forEachObject(function(xxx) { print(xxx.id); print(\"\\n\");}, \"java.io.File\")", printObject)
    }
}