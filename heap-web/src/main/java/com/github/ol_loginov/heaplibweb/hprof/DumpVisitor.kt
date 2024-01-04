package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.hprof.views.ClassDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.InstanceDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.ObjectArrayDumpView
import com.github.ol_loginov.heaplibweb.hprof.views.PrimitiveArrayDumpView
import com.github.ol_loginov.heaplibweb.support.LazyReset
import com.github.ol_loginov.heaplibweb.support.pretty
import com.github.ol_loginov.heaplibweb.support.tuple
import org.slf4j.LoggerFactory

interface DumpVisitor {
    fun onSkip(view: DumpView) {}

    fun onRootUnknown(view: RootUnknownDumpView) = onSkip(view)
    fun onRootJniGlobal(view: RootJniGlobalDumpView) = onSkip(view)
    fun onRootJniLocal(view: RootJniLocalDumpView) = onSkip(view)
    fun onRootJavaFrame(view: RootJavaFrameDumpView) = onSkip(view)
    fun onRootNativeStack(view: RootNativeStackDumpView) = onSkip(view)
    fun onRootStickyClass(view: RootStickyClassDumpView) = onSkip(view)
    fun onRootThreadBlock(view: RootThreadBlockDumpView) = onSkip(view)
    fun onRootMonitorUsed(view: RootMonitorUsedDumpView) = onSkip(view)
    fun onRootThreadObject(view: RootThreadObjectDumpView) = onSkip(view)
    fun onClassDump(view: ClassDumpView) = onSkip(view)
    fun onInstanceDump(view: InstanceDumpView) = onSkip(view)
    fun onObjectArrayDump(view: ObjectArrayDumpView) = onSkip(view)
    fun onPrimitiveArrayDump(view: PrimitiveArrayDumpView) = onSkip(view)
}

inline fun <T : DumpView> visit(view: T, handler: (T) -> Unit) {
    view.reset()
    handler(view)
    view.skip()
}

class DumpVisit {
    companion object {
        val log = LoggerFactory.getLogger(DumpVisit::class.java)
        val scanLogger = PeriodicLogger(2000L, log.isInfoEnabled)
    }

    fun visitAll(reader: HprofRecordReader, visitor: DumpVisitor) {
        val typeCounters = IntArray(SubRecordType.COUNT)
        val scanStart = System.currentTimeMillis()

        val rootUnknown = RootUnknownDumpView(reader)
        val rootJniGlobal = RootJniGlobalDumpView(reader)
        val rootJniLocal = RootJniLocalDumpView(reader)
        val rootJavaFrame = RootJavaFrameDumpView(reader)
        val rootNativeStack = RootNativeStackDumpView(reader)
        val rootStickyClass = RootStickyClassDumpView(reader)
        val rootThreadBlock = RootThreadBlockDumpView(reader)
        val rootMonitorUsed = RootMonitorUsedDumpView(reader)
        val rootThreadObject = RootThreadObjectDumpView(reader)
        val classDump = ClassDumpView(reader)
        val instanceDump = InstanceDumpView(reader)
        val objectArrayDump = ObjectArrayDumpView(reader)
        val primitiveArrayDump = PrimitiveArrayDumpView(reader)

        while (reader.available()) {
            val position = reader.position
            val dumpType = SubRecordType.byTag(reader.ubyte())
            log.debug("dump {} at offset {}", dumpType, position)
            typeCounters[dumpType.ordinal] += 1

            when (dumpType) {
                SubRecordType.GC_ROOT_UNKNOWN -> visit(rootUnknown, visitor::onRootUnknown)
                SubRecordType.GC_ROOT_JNI_GLOBAL -> visit(rootJniGlobal, visitor::onRootJniGlobal)
                SubRecordType.GC_ROOT_JNI_LOCAL -> visit(rootJniLocal, visitor::onRootJniLocal)
                SubRecordType.GC_ROOT_JAVA_FRAME -> visit(rootJavaFrame, visitor::onRootJavaFrame)
                SubRecordType.GC_ROOT_NATIVE_STACK -> visit(rootNativeStack, visitor::onRootNativeStack)
                SubRecordType.GC_ROOT_STICKY_CLASS -> visit(rootStickyClass, visitor::onRootStickyClass)
                SubRecordType.GC_ROOT_THREAD_BLOCK -> visit(rootThreadBlock, visitor::onRootThreadBlock)
                SubRecordType.GC_ROOT_MONITOR_USED -> visit(rootMonitorUsed, visitor::onRootMonitorUsed)
                SubRecordType.GC_ROOT_THREAD_OBJ -> visit(rootThreadObject, visitor::onRootThreadObject)
                SubRecordType.GC_CLASS_DUMP -> visit(classDump, visitor::onClassDump)
                SubRecordType.GC_INSTANCE_DUMP -> visit(instanceDump, visitor::onInstanceDump)
                SubRecordType.GC_OBJ_ARRAY_DUMP -> visit(objectArrayDump, visitor::onObjectArrayDump)
                SubRecordType.GC_PRIM_ARRAY_DUMP -> visit(primitiveArrayDump, visitor::onPrimitiveArrayDump)
            }

            // code below - just to make pretty log entry
            scanLogger {
                val typeNameCounters = typeCounters
                    .mapIndexed { i, v -> SubRecordType.byOrdinal(i) to v }
                    .filter { pair -> pair.second > 0 }
                    .associate { pair -> pair.first to pair.second.pretty() }
                val scanFinish = System.currentTimeMillis()
                log.info("scan dumps complete {} bytes (read in {} sec): {}", reader.position.pretty(), ((scanFinish - scanStart) / 1000.0).pretty(), typeNameCounters)
            }
        }
    }
}

abstract class DumpView(var reader: HprofStreamReader, val dumpType: SubRecordType) {
    abstract fun reset()
    protected fun clearAll(vararg lazys: LazyReset<out Any>) {
        lazys.forEach { it.clear() }
    }

    abstract fun skip()
    protected fun skip(length: Int, unless: Boolean) {
        if (unless) return
        reader.skip(length)
    }
}

class LazyFixedData<T>(val length: Int, factory: () -> T) : LazyReset<T>(factory)

class RootUnknownDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_UNKNOWN) {
    private val _data = LazyFixedData(reader.identifierSize) { reader.id() }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId: ULong get() = _data.value
}

class RootJniGlobalDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_JNI_GLOBAL) {
    private val _data = LazyFixedData(reader.identifierSize * 2) { tuple(reader.id(), reader.id()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val jniGlobalRefID get() = _data.value.second
}

class RootJniLocalDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_JNI_LOCAL) {
    private val _data = LazyFixedData(reader.identifierSize + 4 + 4) { tuple(reader.id(), reader.uint(), reader.uint()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val threadSN get() = _data.value.second
    val frame get() = _data.value.third
}

class RootJavaFrameDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_JAVA_FRAME) {
    private val _data = LazyFixedData(reader.identifierSize + 4 + 4) { tuple(reader.id(), reader.uint(), reader.uint()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val threadSN get() = _data.value.second
    val frame get() = _data.value.third
}

class RootNativeStackDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_NATIVE_STACK) {
    private val _data = LazyFixedData(reader.identifierSize + 4) { tuple(reader.id(), reader.uint()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val threadSN get() = _data.value.second
}

class RootStickyClassDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_STICKY_CLASS) {
    private val _data = LazyFixedData(reader.identifierSize) { reader.id() }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId: ULong get() = _data.value
}

class RootThreadBlockDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_THREAD_BLOCK) {
    private val _data = LazyFixedData(reader.identifierSize + 4) { tuple(reader.id(), reader.uint()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val threadSN get() = _data.value.second
}

class RootMonitorUsedDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_MONITOR_USED) {
    private val _data = LazyFixedData(reader.identifierSize) { reader.id() }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId: ULong get() = _data.value
}

class RootThreadObjectDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_ROOT_THREAD_OBJ) {
    private val _data = LazyFixedData(reader.identifierSize + 4 + 4) { tuple(reader.id(), reader.uint(), reader.uint()) }

    override fun reset() = clearAll(_data)
    override fun skip() = skip(_data.length, _data.isInitialized())

    val objectId get() = _data.value.first
    val threadSN get() = _data.value.second
    val frame get() = _data.value.third
}
