package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.TestTool
import com.github.ol_loginov.heaplibweb.TestTool._when
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.netbeans.lib.profiler.heap.FastHprofHeap
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.script.ScriptEngineManager

private val log: Logger = LoggerFactory.getLogger(NetbeansOQLEngineTest::class.java)

@ContextConfiguration(classes = [InputLoaderBTest.TestContext::class])
@Disabled("manual run")
@Rollback(false)
class NetbeansOQLEngineTest : DatabaseTest() {
    companion object {
        init {
            ScriptEngineManager().getEngineByName("JavaScript") ?: throw IllegalStateException("JavaScript script engine is not available")
        }
    }

    private lateinit var instance: NetbeansOQLEngineForTest

    @Inject
    private lateinit var inputFilesManager: InputFilesManager

    @Inject
    private lateinit var inputLoaderProvider: ObjectProvider<InputLoader>

    private var startTime: Instant? = null

    @BeforeEach
    fun startTIme() {
        startTime = Instant.now()
    }

    @AfterEach
    fun endTime() {
        log.info("test complete in {} secs", Duration.between(startTime, Instant.now()).toSeconds())
    }

    @Test
    fun loadData() {
        val inputFileName = "heapdumps/remote-jdbc-1703107559883.sanitized.hprof"
        _when(inputFilesManager.resolveInputFilePath(inputFileName)).thenReturn(TestTool.getResourceFile(inputFileName).toPath())

        val heapFile = heapFileRepository.persist(HeapFile(inputFileName))
        val work = inputLoaderProvider.getObject()
        work.withFile(heapFile.id)
        work.run()
    }

    @Test
    fun testSuite() {
        val heap = heapFileRepository.findAllByOrderByLoadStartDesc().first()
        val heapProxy = HeapProxy(heapFileRepository.getHeapRepositories(heap))

        instance = NetbeansOQLEngineForTest(heapProxy)
        log.info("testAltTypeNames")
        testAltTypeNames()
        log.info("testIntResult")
        testIntResult()
        log.info("testClassFields")
        testClassFields()
        log.info("testObjectClass")
        testObjectClass()
        log.info("testHeapForEachClass")
        testHeapForEachClass()
        log.info("testHeapForEachObject")
        testHeapForEachObject()
        log.info("testHeapFindObject")
        testHeapFindObject()
        log.info("testHeapRoots")
        testHeapRoots()
        log.info("testHeapClasses")
        testHeapClasses()
        log.info("testHeapFinalizables")
        testHeapFinalizables()
        log.info("testHeapLivePaths")
        testHeapLivePaths()
        log.info("testHeapObjects")
        testHeapObjects()
        log.info("testSubclasses")
        testSubclasses()
        log.info("testSuperclasses")
        testSuperclasses()
        log.info("testForEachReferrer")
        testForEachReferrer()
        log.info("testForEachReferee")
        testForEachReferee()
        log.info("testReferrersInstance")
        testReferrersInstance()
        log.info("testRefereesInstance")
        testRefereesInstance()
        testRefereesClass()
        testRefers()
        testReachables()
        testInstanceOf()
        testSizeOf()
        testRoot()
        testContains()
        testMap()
        testFilter()
        testSort()
        testLength()
        testCountNoClosure()
        testCount()
        testMultivalue()
        testComplexStatement1()
        testComplexStatement2()
        testMapWrapping()
        testUnwrapIterator()
        testUnwrapIteratorComplex()
        testTop()
    }

    protected fun testAltTypeNames() {
        instance.executeQuery("select a from [I a")
        instance.executeQuery("select a from [B a")
        instance.executeQuery("select a from [C a")
        instance.executeQuery("select a from [S a")
        instance.executeQuery("select a from [J a")
        instance.executeQuery("select a from [F a")
        instance.executeQuery("select a from [Z a")
        instance.executeQuery("select a from [java.lang.String a")
    }

    protected fun testIntResult() {
        val tmp = AtomicBoolean(true)
        instance.executeQuery("select a.count from java.lang.String a") {
            if (it !is Int)
                tmp.set(false)
            it !is Int
        }
        assertThat(tmp).isTrue
    }

    protected fun testClassFields() {
        println("test class fields")
        val values = arrayOf("", "prefixLength = int")
        instance.executeQueryAll("select map(heap.findClass(\"java.io.File\").fields, 'toHtml(it.name) + \" = \" + toHtml(it.signature)')") { values[0] = it.toString() }
        assertThat(values[1]).isEqualTo(values[0])
    }

    protected fun testObjectClass() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("test object class accessor")
        instance.executeQuery("select map(a.clazz.statics, 'toHtml(it)') from java.lang.String a")
    }

    protected fun testHeapForEachClass() {
        println("heap.forEachClass")
        val query = "select heap.forEachClass(function(xxx) { print(xxx.name); print(\"\\n\");})"
        instance.executeQuery(query)
    }

    protected fun testHeapForEachObject() {
        println("heap.forEachObject")
        val query = "select heap.forEachObject(function(xxx) { print(xxx.id); print(\"\\n\");}, \"java.io.File\")"
        instance.executeQuery(query)
    }

    protected fun testHeapFindObject() {
        println("heap.findObject")
        val counter = IntArray(1)
        instance.executeQueryAll("select heap.findObject(1684166976)") { counter[0]++ }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testHeapRoots() {
        println("heap.roots")
        val counter = IntArray(1)
        instance.executeQueryAll("select heap.roots") { counter[0]++ }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testHeapClasses() {
        println("heap.classes")
        val counter = IntArray(1)
        instance.executeQueryAll("select heap.classes") { counter[0]++ }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testHeapFinalizables() {
        println("heap.finalizables")
        val counter = IntArray(1)
        instance.executeQueryAll("select heap.finalizables") { counter[0]++ }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testHeapLivePaths() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("heap.livepaths")
        val counter = IntArray(1)
        instance.executeQuery("select heap.livepaths(s) from java.lang.String s") {
            if (it != null)
                counter[0]++
            it != null
        }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testHeapObjects() {
        println("heap.objects")
        val count = intArrayOf(0, 0)
        instance.executeQueryAll("select heap.objects(\"java.io.InputStream\", true)") { count[0]++ }
        instance.executeQueryAll("select heap.objects(\"java.io.InputStream\", false)") { count[1]++ }
        assertThat(count[0]).isNotSameAs(count[1])
        assertThat(count[0]).isEqualTo(4)
        assertThat(count[1]).isEqualTo(0)
    }

    protected fun testSubclasses() {
        println("subclasses")
        val counter = IntArray(1)
        instance.executeQueryOnce("select heap.findClass(\"java.io.InputStream\").subclasses()") {
            println((it as JavaClass).name)
            counter[0]++
        }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testSuperclasses() {
        println("superclasses")
        val counter = IntArray(1)
        instance.executeQueryAll("""select heap.findClass("java.io.DataInputStream").superclasses()""") {
            println((it as JavaClass).name)
            counter[0]++
        }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testForEachReferrer() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("forEachReferrer")
        instance.executeQuery("""select forEachReferrer(function(xxx) { print("referrer: " + xxx.id); print("\n");}, heap.findObject(1684166976))""")
    }

    protected fun testForEachReferee() {
        println("forEachReferee")
        instance.executeQuery("""select forEachReferee(function(xxx) { print("referee: " + xxx.id); print("\n");}, heap.findObject(1684166976))""")
    }

    protected fun testReferrersInstance() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("referrers-instance")
        val query = "select referrers(heap.findObject(1684166976))"
        val referrersTest = longArrayOf(1684166952)
        val referrers: MutableList<Long> = ArrayList()
        instance.executeQueryOnce(query) { referrers.add((it as Instance).instanceId) }
        assertThat(referrersTest).hasSize(referrers.size)
        for (referee: Long in referrersTest) {
            if (!referrers.contains(referee)) org.junit.jupiter.api.Assertions.fail<Any>()
        }
    }

    protected fun testRefereesInstance() {
        println("referees-instance")
        val query = "select referees(heap.findObject(1684166976))"
        val refereesTest = longArrayOf(1684166992)
        val referees: MutableList<Long> = ArrayList()
        instance.executeQueryOnce(query) {
            referees.add((it as Instance).instanceId)
        }
        assertThat(refereesTest).hasSize(referees.size)
        for (referee: Long in refereesTest) {
            if (!referees.contains(referee)) org.junit.jupiter.api.Assertions.fail<Any>()
        }
    }

    protected fun testRefereesClass() {
        println("referees-class")
        val query = "select referees(heap.findClass(\"java.io.File\"))"
        val refereesTest = longArrayOf(1684106928, 1684106888, 1684106848, 1684106408)
        val referees: MutableList<Long> = ArrayList()
        instance.executeQueryOnce(query) { referees.add((it as Instance).instanceId) }
        assertThat(refereesTest).hasSize(referees.size)
        for (referee: Long in refereesTest) {
            if (!referees.contains(referee)) org.junit.jupiter.api.Assertions.fail<Any>()
        }
    }

    protected fun testRefers() {
        println("refers")

        val result = BooleanArray(1)
        instance.executeQueryAll("select refers(heap.findObject(1684166976), heap.findObject(1684166992))") { result[0] = it as Boolean }
        assertThat(result[0]).isTrue

        instance.executeQueryAll("select refers(heap.findObject(1684166992), heap.findObject(1684166976))") { result[0] = it as Boolean }
        assertThat(result[0]).isFalse
    }

    protected fun testReachables() {
        println("reachables")
        val count = IntArray(1)
        instance.executeQueryAll("select reachables(p) from java.util.Properties p") { count[0]++ }
        assertThat(count[0]).isEqualTo(352)
    }

    protected fun testInstanceOf() {
        println("instanceof")
        val counter = IntArray(1)
        instance.executeQueryOnce("select classof(cl).name from instanceof java.lang.ClassLoader cl") {
            println(it)
            counter[0]++
        }
        assertThat(counter[0]).isEqualTo(2) // although there is 8 subclasses of ClassLoader only 2 of them have instances
    }

    protected fun testSizeOf() {
        println("sizeof")
        val counter = IntArray(1)
        instance.executeQueryOnce("select sizeof(o) from [I o") { if (it is Number) counter[0]++ }
        assertThat(counter[0]).isGreaterThan(0)
    }

    protected fun testRoot() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("root")
        val count = IntArray(1)
        instance.executeQueryOnce("select root(heap.findObject(1684166976))") { count[0]++ }
        assertThat(count[0]).isGreaterThan(0)
    }

    protected fun testContains() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("contains")
        val count = IntArray(1)
        instance.executeQueryAll("select s from java.lang.String s where contains(referrers(s), \"classof(it).name == 'java.lang.Class'\")") { count[0]++ }
        assertThat(count[0]).isGreaterThan(0)
    }

    protected fun testMap() {
        assertThat(instance.heap).isNotInstanceOf(FastHprofHeap::class.java)
        println("map")
        val output = arrayOf(
            "",
            "\$assertionsDisabled=true\nserialVersionUID=301077366599181570\ntmpdir=null\ncounter=-1\ntmpFileLock=<a href='file://instance/1684106928' name='1684106928'>java.lang.Object#6</a>\npathSeparator=<a href='file://instance/1684106888' name='1684106888'>java.lang.String#101</a>\npathSeparatorChar=:\nseparator=<a href='file://instance/1684106848' name='1684106848'>java.lang.String#100</a>\nseparatorChar=/\nfs=<a href='file://instance/1684106408' name='1684106408'>java.io.UnixFileSystem#1</a>\n<classLoader>=null\n"
        )
        instance.executeQueryOnce("select map(heap.findClass(\"java.io.File\").statics, \"index + '=' + toHtml(it)\")") { output[0] += it.toString() + "\n" }
        assertThat(output[1]).isEqualTo(output[0])
    }

    protected fun testFilter() {
        println("filter")
        val size = AtomicInteger(0)
        val sorted = AtomicBoolean(true)
        instance.executeQuery("select map(sort(filter(heap.objects('[C'), 'it.length > 0'), 'sizeof(lhs) - sizeof(rhs)'), \"sizeof(it)\")") { o: Any ->
            val aSize: Int = (o as Number).toInt()
            if (aSize < size.get()) {
                sorted.set(false)
                return@executeQuery true
            }
            size.set(aSize)
            false
        }
        assertThat(sorted).isTrue
    }

    protected fun testSort() {
        println("sort")
        val size = AtomicInteger(0)
        val sorted = AtomicBoolean(true)
        instance.executeQuery("select map(sort(heap.objects('[C'), 'sizeof(lhs) - sizeof(rhs)'), \"sizeof(it)\")") {
            val aSize: Int = (it as Number).toInt()
            if (aSize < size.get()) {
                sorted.set(false)
                true
            } else {
                size.set(aSize)
                false
            }
        }
        assertThat(sorted).isTrue
    }

    protected fun testLength() {
        println("length")
        val rsltClass = AtomicReference<Class<*>>()
        //        final boolean sorted[] = new boolean[] {true};
        instance.executeQueryOnce("select length(a.value) from java.lang.String a") { rsltClass.set(it?.javaClass) }
        assertThat(rsltClass.get()).isAssignableTo(Number::class.java)
    }

    protected fun testCountNoClosure() {
        println("count - no closure")
        val rsltClass = AtomicReference<Class<*>>()
        instance.executeQueryAll("select count(a.value) from java.lang.String a") { rsltClass.set(it?.javaClass) }
        assertThat(rsltClass.get()).isAssignableTo(Number::class.java)
    }

    protected fun testCount() {
        println("count")
        val rsltClass = AtomicReference<Class<*>>()
        instance.executeQueryAll("select count(a.value, 'true') from java.lang.String a") { rsltClass.set(it?.javaClass) }
        assertThat(rsltClass.get()).isInstanceOf(Double::class.java)
    }

    protected fun testMultivalue() {
        println("multi-value")
        val rsltClass = AtomicReference<Class<*>>()
        instance.executeQueryAll("select { name: t.name? t.name.toString() : \"null\", thread: t }  from instanceof java.lang.Thread t") { rsltClass.set(it?.javaClass) }
        assertThat(rsltClass.get()).isAssignableTo(MutableMap::class.java)
    }

    protected fun testComplexStatement1() {
        println("complex statement 1")
        val res = AtomicReference<String>()
        instance.executeQueryAll(
            """
              select map(filter(heap.findClass('java.lang.System').statics.props.table, 'it != null && it.key != null && it.value != null'),
                function (it) {
                    return 'MapEntry{' + it.key.toString() + ' = ' + it.value.toString() + '}';
                })
            """
        ) {
            println(it)
            res.set(it.toString())
        }
        assertThat(res.get()).isEqualTo("MapEntry{sun.cpu.isalist = }")
    }

    protected fun testComplexStatement2() {
        println("complex statement 2")
        val tmp = AtomicReference<String>()
        instance.executeQueryAll(
            """
              select map(filter(heap.findClass('java.lang.System').statics.props.table, 'it != null && it.key != null && it.value != null'), '{ key: it.key.toString(), value: it.value.toString() }')   
            """
        ) {
            println(it)
            tmp.set(it.toString())
        }
        assertThat(tmp.get()).isEqualTo("{value=, key=sun.cpu.isalist}")
    }

    protected fun testMapWrapping() {
        println("map wrapping")
        val result = arrayOf("", "<a href='file://class/1746081976' name='1746081976'>class java.util.HashMap\$Entry[]</a>")
        instance.executeQueryAll("select unique(map(filter(reachables(a), 'it != null'), 'toHtml(it.clazz)')) from instanceof java.util.HashMap a") { result[0] = it.toString() }
        assertThat(result[1]).isEqualTo(result[0])
    }

    protected fun testUnwrapIterator() {
        println("unwrap iterator")
        instance.executeQueryAll("select map(filter(a.table, 'it != null'), 'reachables(it)') from instanceof java.util.HashMap a") { println(it) }
    }

    protected fun testUnwrapIteratorComplex() {
        println("unwrap iterator complex")
        instance.executeQueryAll("select map(map(filter(a.table, 'it != null'), 'reachables(it)'), 'it.clazz.statics') from instanceof java.util.HashMap a") { println(it) }
    }

    protected fun testTop() {
        println("top 5")
        instance.executeQueryOnce(
            """
      select top(heap.objects('java.lang.String', false, '(2 * it.offset) + (2 * (it.value.length - (1*it.count + 1*it.offset))) > 0'), '((2 * rhs.offset) + (2 * (rhs.value.length - (1*rhs.count + 1*rhs.offset)))) - ((2 * lhs.offset) + (2 * (lhs.value.length - (1*lhs.count + 1*lhs.offset))))')          
            """,
        ) { println(it) }
    }
}

