package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.support.void
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.StopWatch
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.Path

private val log = LoggerFactory.getLogger(HprofStreamBTest::class.java)

@ActiveProfiles("local", "test", "test-local")
@SpringBootTest(classes = [HprofStreamBTest.TestContext::class])
class HprofStreamBTest {
    class TestContext

    @Inject
    private lateinit var environment: Environment

    @Test
    @Disabled("manual execution")
    fun scrollHugeDump() {
        val hugeDumpFile = Path(environment.get("test.huge-dump") ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val hprof = HprofStream(hugeDumpFile)
        hprof.scanClasses()
    }

    @Test
    @Disabled("manual execution")
    fun hugeScanDumps() {
        val hugeDumpFile = Path(environment.get("test.huge-dump") ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val iterations = 4
        val (instances, pArray, oArray) = testScanDumps(hugeDumpFile, iterations)
        assertThat(instances / iterations).isEqualTo(183848756L)
        assertThat(pArray / iterations).isEqualTo(23501490L)
        assertThat(oArray / iterations).isEqualTo(10911562L)
    }

    private fun testScanDumps(file: Path, iterations: Int): Array<Long> {
        log.info("use $file")
        val instances = AtomicLong()
        val pArray = AtomicLong()
        val oArray = AtomicLong()

        val hprof = HprofStream(file)
        val stopWatch = StopWatch(file.toString())
        for (i in 1..iterations) {
            log.info("*************** iteration $i **********************")
            stopWatch.start("test $i")
            hprof.scanDumps(object : DumpReceiver {
                override fun onInstance(dump: InstanceDump, fieldReader: InstanceFieldReader) = instances.incrementAndGet().void()
                override fun onPrimitiveArray(dump: PrimitiveArrayDump) = pArray.incrementAndGet().void()
                override fun onObjectArray(dump: ObjectArrayDump) = oArray.incrementAndGet().void()
            })
            stopWatch.stop()
        }
        log.info(stopWatch.prettyPrint())
        return arrayOf(instances.get(), pArray.get(), oArray.get())
    }

    @Test
    fun scrollTestumDump() {
        val (instances, pArray, oArray) = testScanDumps(
            TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(),
            10
        )
        assertThat(instances / 10).isEqualTo(19248L)
        assertThat(pArray / 10).isEqualTo(10379L)
        assertThat(oArray / 10).isEqualTo(2871L)
    }
}