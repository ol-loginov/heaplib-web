package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.support.LazyReset
import com.github.ol_loginov.heaplibweb.support.pretty
import org.slf4j.LoggerFactory

interface RecordVisitor {
    fun onUTF8(view: UTF8RecordView) = onSkip(view)
    fun onLoadClass(view: LoadClassRecordView) = onSkip(view)
    fun onUnloadClass(view: UnloadClassRecordView) = onSkip(view)
    fun onFrame(view: FrameRecordView) = onSkip(view)
    fun onTrace(view: TraceRecordView) = onSkip(view)
    fun onDumpSegment(view: DumpSegmentRecordView) = onSkip(view)
    fun onSkip(view: RecordView) {}
}

class RecordVisit {
    companion object {
        val log = LoggerFactory.getLogger(RecordVisit::class.java)
        val scanLogger = PeriodicLogger(10000L, log.isInfoEnabled)
    }

    var recordsCount = 0

    fun visitAll(reader: HprofFileReader, recordVisitor: RecordVisitor) {
        log.info("scan records")
        val scanStart = System.currentTimeMillis()

        val typeCounters = IntArray(RecordType.COUNT)

        val utF8RecordView = UTF8RecordView()
        val loadClassRecordView = LoadClassRecordView()
        val unloadClassRecordView = UnloadClassRecordView()
        val frameRecordView = FrameRecordView()
        val traceRecordView = TraceRecordView()
        val dumpSegmentRecordView = DumpSegmentRecordView()

        do {
            val record = HprofRecordReader(reader)
            log.debug("record {} of length {} at offset {}", record.recordType, record.length, reader.position)
            typeCounters[record.recordType.ordinal] += 1

            when (record.recordType) {
                RecordType.UTF8 -> recordVisitor.onUTF8(RecordView.reset(utF8RecordView, record))
                RecordType.LOAD_CLASS -> recordVisitor.onLoadClass(RecordView.reset(loadClassRecordView, record))
                RecordType.UNLOAD_CLASS -> recordVisitor.onUnloadClass(RecordView.reset(unloadClassRecordView, record))
                RecordType.FRAME -> recordVisitor.onFrame(RecordView.reset(frameRecordView, record))
                RecordType.TRACE -> recordVisitor.onTrace(RecordView.reset(traceRecordView, record))
                RecordType.HEAP_DUMP_SEGMENT,
                RecordType.HEAP_DUMP -> recordVisitor.onDumpSegment(RecordView.reset(dumpSegmentRecordView, record))

                else -> {}
            }

            record.skipToEnd()

            scanLogger {
                val typeNameCounters = typeCounters
                    .mapIndexed { i, v -> RecordType.byOrdinal(i) to v }
                    .filter { pair -> pair.second > 0 }
                    .associate { pair -> pair.first to pair.second.pretty() }
                val scanFinish = System.currentTimeMillis()
                log.info("scan records complete ({} bytes read in {} sec): {}", reader.position.pretty(), ((scanFinish - scanStart) / 1000.0).pretty(), typeNameCounters)
            }
        } while (reader.available())

        recordsCount = typeCounters.sum()
    }
}

abstract class RecordView(val recordType: RecordType) {
    companion object {
        fun <T : RecordView> reset(view: T, backend: HprofRecordReader): T {
            view.readerRef = backend
            view.reset()
            return view
        }
    }

    protected var readerRef: HprofRecordReader? = null
    protected val reader: HprofRecordReader
        get() = readerRef!!

    protected open fun reset() {}
    protected fun clearAll(vararg lazyValues: LazyReset<out Any>) {
        lazyValues.forEach { it.clear() }
    }
}

class UTF8RecordView : RecordView(RecordType.UTF8) {
    private val _id = LazyReset { reader.id() }
    private val _string = LazyReset { _id.initialize(); reader.string(reader.end - reader.position) }

    override fun reset() = clearAll(_id, _string)

    val id: ULong get() = _id.value
    val string: String get() = _string.value
}

class LoadClassRecordView : RecordView(RecordType.LOAD_CLASS) {
    private val _classSN = LazyReset { reader.uint() }
    private val _classObjectId = LazyReset { _classSN.initialize(); reader.id() }
    private val _stackTraceSN = LazyReset { _classObjectId.initialize(); reader.uint() }
    private val _nameId = LazyReset { _stackTraceSN.initialize(); reader.id() }

    override fun reset() = clearAll(_classSN, _classObjectId, _stackTraceSN, _nameId)

    val classSN: UInt get() = _classSN.value
    val classObjectId: ULong get() = _classObjectId.value
    val stackTraceSN: UInt get() = _stackTraceSN.value
    val nameId: ULong get() = _nameId.value
}

class UnloadClassRecordView : RecordView(RecordType.UNLOAD_CLASS)
class FrameRecordView : RecordView(RecordType.FRAME)
class TraceRecordView : RecordView(RecordType.TRACE)

class DumpSegmentRecordView : RecordView(RecordType.HEAP_DUMP_SEGMENT) {
    fun scan(visitor: DumpVisitor) {
        val visit = DumpVisit()
        visit.visitAll(reader, visitor)
    }
}

