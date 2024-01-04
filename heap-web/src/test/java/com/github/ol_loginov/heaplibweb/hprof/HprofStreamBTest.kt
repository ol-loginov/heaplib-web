package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.TestTool
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.StopWatch
import java.nio.file.Path
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
    fun hugeScanDumps() {
        val hugeDumpFile = Path(environment["test.huge-dump"] ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val dumps = testScanDumps(hugeDumpFile, 4)
        assertThat(dumps).containsOnly(
            entry(SubRecordType.GC_PRIM_ARRAY_DUMP, 704566),
        )
    }

    @Test
    @Disabled("manual execution")
    fun hugeScanRecords() {
        val hugeDumpFile = Path(environment["test.huge-dump"] ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val records = testScanRecords(hugeDumpFile, 1)
        assertThat(records).containsOnly(
            entry(RecordType.UTF8, 704566),
            entry(RecordType.LOAD_CLASS, 952426),
            entry(RecordType.FRAME, 35173),
            entry(RecordType.TRACE, 2500),
            entry(RecordType.HEAP_DUMP_SEGMENT, 17904)
        )
    }

    @Test
    @Disabled("manual execution")
    fun hugeScanDumps1() {
        val hugeDumpFile = Path(environment["test.huge-dump"] ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val dumps = testScanDumps(hugeDumpFile, 1)
        assertThat(dumps).containsOnly(
            entry(SubRecordType.GC_CLASS_DUMP, 41117),
        )
    }

    private fun testScanDumps(file: Path, iterations: Int): MutableMap<SubRecordType, Int> {
        val counters = mutableMapOf<SubRecordType, Int>()
        log.info("use $file")

        val hprof = HprofFile(file)
        val stopWatch = StopWatch(file.toString())
        for (i in 1..iterations) {
            counters.clear()
            fun increment(key: SubRecordType) {
                counters.compute(key) { _, b -> 1 + (b ?: 0) }
            }

            log.info("*************** iteration $i **********************")
            counters.clear()
            stopWatch.start("test $i")
            hprof.scanDumps(object : DumpVisitor {
                override fun onSkip(view: DumpView) = increment(view.dumpType)
            })
            stopWatch.stop()
        }
        log.info(stopWatch.prettyPrint())
        return counters
    }

    private fun testScanRecords(file: Path, iterations: Int): Map<RecordType, Int> {
        val counters = mutableMapOf<RecordType, Int>()
        log.info("use $file")

        val hprof = HprofFile(file)
        val stopWatch = StopWatch(file.toString())
        for (i in 1..iterations) {
            counters.clear()
            fun increment(recordType: RecordType) {
                counters.compute(recordType) { _, b -> 1 + (b ?: 0) }
            }

            log.info("*************** iteration $i **********************")
            stopWatch.start("test $i")
            hprof.scanRecords(object : RecordVisitor {
                override fun onSkip(view: RecordView) = increment(view.recordType)
            })
            stopWatch.stop()
        }
        log.info(stopWatch.prettyPrint())
        return counters
    }

    @Test
    fun testumScanRecords10() {
        val records = testScanRecords(TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(), 10)
        assertThat(records).containsOnly(
            entry(RecordType.UTF8, 41117),
            entry(RecordType.LOAD_CLASS, 992),
            entry(RecordType.FRAME, 14),
            entry(RecordType.TRACE, 8),
            entry(RecordType.HEAP_DUMP_SEGMENT, 8)
        )
    }

    @Test
    fun testumScanDumps10() {
        val dumps = testScanDumps(TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(), 10)
        assertThat(dumps).containsOnly(
            entry(SubRecordType.GC_CLASS_DUMP, 867),
            entry(SubRecordType.GC_PRIM_ARRAY_DUMP, 10379),
            entry(SubRecordType.GC_INSTANCE_DUMP, 19248),
            entry(SubRecordType.GC_OBJ_ARRAY_DUMP, 2871),
            entry(SubRecordType.GC_ROOT_THREAD_OBJ, 7),
            entry(SubRecordType.GC_ROOT_JAVA_FRAME, 14),
            entry(SubRecordType.GC_ROOT_JNI_GLOBAL, 54),
            entry(SubRecordType.GC_ROOT_STICKY_CLASS, 729),
        )
    }

    @Test
    fun testumScanDumps1() {
        val dumps = testScanDumps(TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(), 1)
        assertThat(dumps).containsOnly(
            entry(SubRecordType.GC_CLASS_DUMP, 867),
            entry(SubRecordType.GC_PRIM_ARRAY_DUMP, 10379),
            entry(SubRecordType.GC_INSTANCE_DUMP, 19248),
            entry(SubRecordType.GC_OBJ_ARRAY_DUMP, 2871),
            entry(SubRecordType.GC_ROOT_THREAD_OBJ, 7),
            entry(SubRecordType.GC_ROOT_JAVA_FRAME, 14),
            entry(SubRecordType.GC_ROOT_JNI_GLOBAL, 54),
            entry(SubRecordType.GC_ROOT_STICKY_CLASS, 729),
        )
    }

    private fun testScanClasses(file: Path, iterations: Int): List<ClassDump> {
        var classes: List<ClassDump> = emptyList()
        log.info("use $file")

        val hprof = HprofFile(file)
        val stopWatch = StopWatch(file.toString())
        for (i in 1..iterations) {
            log.info("*************** iteration $i **********************")
            stopWatch.start("test $i")
            classes = hprof.scanClasses()
            stopWatch.stop()
        }
        log.info(stopWatch.prettyPrint())
        return classes
    }

    @Test
    fun testumScanClasses1() {
        val classes = testScanClasses(TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(), 1)
        assertThat(classes).hasSize(867)
        assertThat(classes.count { it.className.orEmpty().startsWith("testum/") }).isEqualTo(6)
        assertThat(classes).anyMatch { it.className.orEmpty() == "testum/ClassA" }
        assertThat(classes).anyMatch { it.className.orEmpty() == "testum/Main" }
    }

    @Test
    fun testumScanClasses10() {
        val classes = testScanClasses(TestTool.getResourceFile("heapdumps/testum-1703615978111.hprof").toPath(), 10)
        assertThat(classes).hasSize(867)
        assertThat(classes.count { it.className.orEmpty().startsWith("testum/") }).isEqualTo(6)
        assertThat(classes).anyMatch { it.className.orEmpty() == "testum/ClassA" }
        assertThat(classes).anyMatch { it.className.orEmpty() == "testum/Main" }
    }


    @Test
    @Disabled("manual execution")
    fun testumHugeClasses1() {
        val hugeDumpFile = Path(environment["test.huge-dump"] ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val classes = testScanClasses(hugeDumpFile, 1)
        assertThat(classes).hasSize(911543)
    }
}
