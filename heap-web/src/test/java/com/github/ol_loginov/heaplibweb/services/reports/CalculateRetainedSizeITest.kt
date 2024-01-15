package com.github.ol_loginov.heaplibweb.services.reports

import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus
import com.github.ol_loginov.heaplibweb.services.TestumDumpBTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [TestumDumpBTest.TestContext::class])
class CalculateRetainedSizeITest : DatabaseTest() {
    @Test
    fun calculateTestum() {
        val testumHeap = heapFileRepository.findAllByOrderByLoadStartDesc()
            .filter { it.status == HeapFileStatus.LOADED }
            .first { it.path.endsWith(TestumDumpBTest.inputFileName) }

        val heapRepositories = heapFileRepository.getHeapRepositories(testumHeap)
        heapRepositories.instances.clearRetainedSize()

        val report = CalculateRetainedSize(heapRepositories)
        report(transactionOperations)
    }
}