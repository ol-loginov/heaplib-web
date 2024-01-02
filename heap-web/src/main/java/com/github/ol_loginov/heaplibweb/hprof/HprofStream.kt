package com.github.ol_loginov.heaplibweb.hprof

import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.text.NumberFormat
import java.time.Instant
import kotlin.io.path.inputStream

/**
 * https://hg.openjdk.org/jdk6/jdk6/jdk/raw-file/tip/src/share/demo/jvmti/hprof/manual.html
 * https://hg.openjdk.org/jdk/jdk/file/9a73a4e4011f/src/hotspot/share/services/heapDumper.cpp
 */
@OptIn(ExperimentalUnsignedTypes::class)
class HprofStream(private val file: Path) {
    companion object {
        private val log = LoggerFactory.getLogger(HprofStream::class.java)

        const val HEAD_MARKER = "JAVA PROFILE 1.0.2"
    }

    private var headLength = 0L
    private var identifierSize: Int = 0
    private var time: Instant = Instant.EPOCH

    private var recordsCount: Int = 0
    private var dumpsCount: Int = 0
    private var scanLogTime: Long = System.currentTimeMillis()

    private fun openDataStream(): DataInputStream = DataInputStream(BufferedInputStream(file.inputStream(), 10_000_000))

    private fun scanHead(data: DataInputStream) {
        val marker = data.readNBytes(HEAD_MARKER.length).toString(StandardCharsets.US_ASCII)
        data.readByte() // and zero-terminator of string above
        if (marker != HEAD_MARKER) throw IllegalArgumentException("not a hprof file")

        identifierSize = data.readInt()
        val timeHighWord = data.readInt().toUInt().toLong()
        val timeLowWord = data.readInt().toUInt().toLong()
        time = Instant.ofEpochMilli((timeHighWord shl 4 * 8) + timeLowWord)

        headLength = HEAD_MARKER.length.toLong() + 1 + 4 * 3
    }

    private fun openReader(consumer: (reader: HprofStreamReader) -> Unit) {
        openDataStream().use {
            scanHead(it)
            consumer(HprofStreamReader(it, identifierSize))
        }
    }

    fun scanClasses(): List<ClassDump> {
        val scanner = ClassDumpScanner(this)
        return scanner.scan()
    }

    fun scanDumps(receiver: DumpReceiver) {
        val scanner = DumpScanner(this, receiver)
        scanner.scan()
    }

    internal fun scan(recordVisitor: RecordVisitor?, dumpVisitor: HeapDumpVisitor?) {
        openReader { reader ->
            scanRecords(reader, recordVisitor, dumpVisitor)
        }
    }

    private fun logScanInfo(runnable: Runnable) {
        if (log.isInfoEnabled && scanLogTime + 1000 < System.currentTimeMillis()) {
            runnable.run()
            scanLogTime = System.currentTimeMillis()
        }
    }

    class ScanProgress(
        val recordsCount: Int,
        var recordsPassed: Int,
        val dumpsCount: Int,
        var dumpsPassed: Int,
        var bytesRead: Long
    ) {
        fun passedRecordsInfo(): String = if (recordsCount == 0) "$recordsPassed" else "$recordsPassed/$recordsCount"
        fun passedDumpsInfo(): String = if (dumpsCount == 0) "$dumpsPassed" else "$dumpsPassed/$dumpsCount"
        fun bytesReadString(bytes: Long): String {
            val bytesReadStringFormat = NumberFormat.getIntegerInstance()
            bytesReadStringFormat.isGroupingUsed = true
            return bytesReadStringFormat.format(bytes) + " bytes"
        }
    }

    private fun scanRecords(reader: HprofStreamReader, recordVisitor: RecordVisitor?, dumpVisitor: HeapDumpVisitor?) {
        log.info("scan records")
        val scanStart = System.currentTimeMillis()
        val actualVisitor = recordVisitor ?: object : RecordVisitor {}

        val typeCounters = IntArray(RecordType.TYPE_LIMIT)
        val bytesRead = reader.bytesRead
        val progress = ScanProgress(recordsCount, 0, dumpsCount, 0, 0)

        do {
            val recordTag = RecordType.valueOf(reader.ubyte())

            @Suppress("UNUSED_VARIABLE")
            val recordTimeOffset = reader.uint()
            val recordLength = reader.uint().toLong()
            val recordReader = reader.limited(recordLength)
            log.debug("record {} of length {} at offset {}", recordTag, recordLength, reader.bytesRead)

            when (recordTag) {
                RecordType.UTF8 -> actualVisitor.onUTF8(recordReader, recordLength)
                RecordType.LOAD_CLASS -> actualVisitor.onLoadClass(recordReader, recordLength)
                RecordType.UNLOAD_CLASS -> actualVisitor.onUnloadClass(recordReader, recordLength)
                RecordType.FRAME -> actualVisitor.onFrame(recordReader, recordLength)
                RecordType.TRACE -> actualVisitor.onTrace(recordReader, recordLength)
                RecordType.HEAP_DUMP_END -> recordReader.skip(recordLength)

                RecordType.HEAP_DUMP_SEGMENT,
                RecordType.HEAP_DUMP -> {
                    if (dumpVisitor == null) {
                        recordReader.skip(recordLength)
                    } else {
                        scanDumpSubRecords(recordReader, dumpVisitor, progress)
                    }
                }

                else -> actualVisitor.skipRecord(recordReader, recordTag, recordLength)
            }

            typeCounters[recordTag.tag.toInt()] += 1
            assert(!recordReader.available())

            reader.bytesRead += recordReader.bytesRead
            progress.bytesRead = reader.bytesRead
        } while (reader.available())

        progress.recordsPassed += typeCounters.sum()

        // code below - just to make pretty log entry
        logScanInfo {
            val typeNameCounters = typeCounters
                .mapIndexed { i, v -> i to v }
                .filter { pair -> pair.second > 0 }
                .associate { pair -> RecordType.typeName(pair.first) to pair.second }
            val scanFinish = System.currentTimeMillis()
            log.info("scan records complete {} ({} bytes read in {} sec): {}", progress.bytesReadString(reader.bytesRead), reader.bytesRead - bytesRead, (scanFinish - scanStart) / 1000.0, typeNameCounters)
        }

        recordsCount = progress.recordsPassed
        if (progress.dumpsPassed > 0) {
            dumpsCount = progress.dumpsPassed
        }
    }

    private fun scanDumpSubRecords(reader: HprofStreamReader, visitor: HeapDumpVisitor, progress: ScanProgress) {
        val typeCounters = IntArray(SubRecordType.TYPE_LIMIT)
        val scanStart = System.currentTimeMillis()

        while (reader.available()) {
            val subRecordType = SubRecordType.valueOf(reader.ubyte())
            typeCounters[subRecordType.tag.toInt()] += 1

            when (subRecordType) {
                SubRecordType.GC_ROOT_UNKNOWN -> visitor.onRootUnknown(reader.id())
                SubRecordType.GC_ROOT_JNI_GLOBAL -> visitor.onRootJniGlobal(reader.id(), reader.id())
                SubRecordType.GC_ROOT_JNI_LOCAL -> visitor.onRootJniLocal(reader.id(), reader.uint(), reader.uint())
                SubRecordType.GC_ROOT_JAVA_FRAME -> visitor.onRootJavaFrame(reader.id(), reader.uint(), reader.uint())
                SubRecordType.GC_ROOT_NATIVE_STACK -> visitor.onRootNativeStack(reader.id(), reader.uint())
                SubRecordType.GC_ROOT_STICKY_CLASS -> visitor.onRootStickyClass(reader.id())
                SubRecordType.GC_ROOT_THREAD_BLOCK -> visitor.onRootThreadBlock(reader.id(), reader.uint())
                SubRecordType.GC_ROOT_MONITOR_USED -> visitor.onRootMonitorUsed(reader.id())
                SubRecordType.GC_ROOT_THREAD_OBJ -> visitor.onRootThreadObject(reader.id(), reader.uint(), reader.uint())
                SubRecordType.GC_CLASS_DUMP -> scanClassDumpSubRecord(reader, visitor)
                SubRecordType.GC_INSTANCE_DUMP -> scanInstanceDumpSubRecord(reader, visitor)
                SubRecordType.GC_OBJ_ARRAY_DUMP -> scanObjectArrayDumpSubRecord(reader, visitor)
                SubRecordType.GC_PRIM_ARRAY_DUMP -> scanPrimitiveArrayDumpSubRecord(reader, visitor)
            }
        }

        progress.dumpsPassed += typeCounters.sum()

        // code below - just to make pretty log entry
        logScanInfo {
            val typeNameCounters = typeCounters
                .mapIndexed { i, v -> i to v }
                .filter { pair -> pair.second > 0 }
                .associate { pair -> SubRecordType.typeName(pair.first) to pair.second }
            val scanFinish = System.currentTimeMillis()
            log.info("scan sub-records complete {} (read in {} sec): {}", progress.bytesReadString(progress.bytesRead + reader.bytesRead), (scanFinish - scanStart) / 1000.0, typeNameCounters)
        }
    }


    private fun scanInstanceDumpSubRecord(reader: HprofStreamReader, visitor: HeapDumpVisitor) {
        val objectId = reader.id()
        val stackTraceSN = reader.uint()
        val classObjectId = reader.id()
        val fieldsData = reader.bytes(reader.int())
        visitor.onGCInstanceDump(InstanceDump(objectId, stackTraceSN, classObjectId, fieldsData))
    }

    private fun scanClassDumpSubRecord(reader: HprofStreamReader, visitor: HeapDumpVisitor) {
        val classObjectId = reader.id()
        val stackTraceSN = reader.uint()
        val superClassObjectId = reader.id()
        val classLoaderObjectId = reader.id()
        val signersObjectId = reader.id()
        val domainObjectId = reader.id()
        reader.id()
        reader.id()
        val instanceSize = reader.uint()

        val constantPool = mutableListOf<ValueRecord>()
        for (constantPoolIt in 0 until reader.ushort().toInt()) {
            reader.ushort()
            constantPool.add(scanTypeAndValueRecord(reader))
        }

        val staticFields = mutableListOf<NamedValue>()
        for (i in 0 until reader.ushort().toInt()) {
            val name = reader.id()
            val value = scanTypeAndValueRecord(reader)
            staticFields.add(NamedValue(StringRef(name), value.type, value.content))
        }

        // without super class
        val instanceFields = mutableListOf<NamedType>()
        for (i in 0 until reader.ushort().toInt()) {
            val name = reader.id()
            instanceFields.add(NamedType(StringRef(name), reader.type()))
        }

        visitor.onGCClassDump(ClassDump(classObjectId, stackTraceSN, superClassObjectId, classLoaderObjectId, signersObjectId, domainObjectId, instanceSize, StringRef(), constantPool, staticFields, instanceFields))
    }

    private fun scanPrimitiveArrayDumpSubRecord(reader: HprofStreamReader, visitor: HeapDumpVisitor) {
        val objectId = reader.id()
        val stackTraceSN = reader.uint()
        val arrayLength = reader.int()
        val type = reader.type()

        val dump = PrimitiveArrayDump(
            objectId, stackTraceSN, type, when (type) {
                HprofValueType.Boolean -> IntRange(1, arrayLength).map { reader.ubyte() }
                HprofValueType.Char -> IntRange(1, arrayLength).map { reader.char() }
                HprofValueType.Float -> IntRange(1, arrayLength).map { reader.float() }
                HprofValueType.Double -> IntRange(1, arrayLength).map { reader.double() }
                HprofValueType.Byte -> IntRange(1, arrayLength).map { reader.byte() }
                HprofValueType.Short -> IntRange(1, arrayLength).map { reader.short() }
                HprofValueType.Int -> IntRange(1, arrayLength).map { reader.int() }
                HprofValueType.Long -> IntRange(1, arrayLength).map { reader.long() }
                else -> throw NotImplementedError("Expect primitive type of array, got $type")
            }
        )
        visitor.onGCPrimitiveArrayDump(dump)
    }

    private fun scanObjectArrayDumpSubRecord(reader: HprofStreamReader, visitor: HeapDumpVisitor) {
        val objectId = reader.id()
        val stackTraceSN = reader.uint()
        val arrayLength = reader.int()
        val itemClassId = reader.id()
        val items = IntRange(1, arrayLength).map { reader.id() }
        visitor.onGCObjectArrayDump(ObjectArrayDump(objectId, stackTraceSN, itemClassId, items))
    }

    private fun scanTypeAndValueRecord(reader: HprofStreamReader): ValueRecord {
        return when (val type = reader.type()) {
            HprofValueType.Boolean -> ValueRecord.boolean(reader.ubyte())
            HprofValueType.Byte -> ValueRecord.byte(reader.byte())
            HprofValueType.Object -> ValueRecord.obj(reader.id())
            HprofValueType.Char -> ValueRecord.char(reader.ushort())
            HprofValueType.Float -> ValueRecord.float(reader.float())
            HprofValueType.Double -> ValueRecord.double(reader.double())
            HprofValueType.Short -> ValueRecord.short(reader.short())
            HprofValueType.Int -> ValueRecord.int(reader.int())
            HprofValueType.Long -> ValueRecord.long(reader.long())
            else -> throw NotImplementedError("type $type is not supported here")
        }
    }
}

