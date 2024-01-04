package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.hprof.views.ClassDumpView
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Instant

private val log = LoggerFactory.getLogger(HprofFile::class.java)
private val scanLogger = PeriodicLogger(2000L, log.isInfoEnabled)
const val HEAD_MARKER = "JAVA PROFILE 1.0.2"

/**
 * https://hg.openjdk.org/jdk6/jdk6/jdk/raw-file/tip/src/share/demo/jvmti/hprof/manual.html
 * https://hg.openjdk.org/jdk/jdk/file/9a73a4e4011f/src/hotspot/share/services/heapDumper.cpp
 */
class HprofFile(private val file: Path) {
    private var identifierSize: Int = 0
    private var time: Instant = Instant.EPOCH

    private var recordsCount: Int = 0

    private fun openDataStream(): HprofFileSource = DISFileSource(file, 50 * 1024 * 1024)
//    private fun openDataStream(): HprofFileSource = RAFFileSource(file)

    private fun scanHead(data: HprofFileSource): Long {
        val marker = data.readNBytes(HEAD_MARKER.length).toString(StandardCharsets.US_ASCII)
        data.readByte() // and zero-terminator of string above
        if (marker != HEAD_MARKER) throw IllegalArgumentException("not a hprof file")

        identifierSize = data.readInt()
        val timeHighWord = data.readInt().toUInt().toLong()
        val timeLowWord = data.readInt().toUInt().toLong()
        time = Instant.ofEpochMilli((timeHighWord shl 4 * 8) + timeLowWord)

        return HEAD_MARKER.length.toLong() + 1 + 4 * 3
    }

    private fun openReader(consumer: (reader: HprofFileReader) -> Unit) {
        openDataStream().use {
            val position = scanHead(it)
            consumer(HprofFileReader(it, identifierSize, position))
        }
    }

    fun scanRecords(recordVisitor: RecordVisitor) {
        openReader { reader ->
            val visit = RecordVisit()
            visit.visitAll(reader, recordVisitor)
            recordsCount = visit.recordsCount
        }
    }

    fun scanDumps(visitor: DumpVisitor) {
        scanRecords(object : RecordVisitor {
            override fun onDumpSegment(view: DumpSegmentRecordView) {
                view.scan(visitor)
            }
        })
    }

    fun scanClasses(): List<ClassDump> {
        log.info("visit load class records")
        val classNames = mutableMapOf<ULong, StringRef>()
        scanRecords(object : RecordVisitor {
            override fun onLoadClass(view: LoadClassRecordView) {
                classNames[view.classObjectId] = StringRef(view.nameId, null)
            }
        })

        log.info("visit class dumps")
        val classDumps = mutableMapOf<ULong, ClassDump>()
        scanDumps(object : DumpVisitor {
            override fun onClassDump(view: ClassDumpView) {
                classDumps[view.classObjectId] = ClassDump(
                    view.classObjectId, view.superClassObjectId, view.classLoaderObjectId,
                    view.instanceSize, classNames[view.classObjectId] ?: StringRef(),
                    view.constantPool, view.staticFields, view.instanceFields
                )
            }
        })

        log.info("update class names")
        val records = classDumps.values.toMutableList()
        val stringReferences = mutableMapOf<ULong, MutableSet<Int>>()

        fun addReference(stringId: ULong, entityIndex: Int) {
            stringReferences.computeIfAbsent(stringId) { mutableSetOf() }.add(entityIndex)
        }

        for ((index, v) in records.withIndex()) {
            addReference(v.className.id, index)
            v.instanceFields.forEach { t ->
                addReference(t.name.id, index)
            }
            v.staticFields.forEach { t ->
                addReference(t.name.id, index)
            }
        }

        scanRecords(object : RecordVisitor {
            override fun onUTF8(view: UTF8RecordView) {
                val id = view.id
                val indices = stringReferences.remove(id) ?: return

                val name = StringRef(id, view.string)
                indices.forEach { index ->
                    var entry = records[index]
                    if (entry.className.id == id) {
                        entry = entry.copy(className = name)
                    }
                    entry = entry.copy(staticFields = entry.staticFields.map { if (it.name.id == id) it.copy(name = name) else it })
                    entry = entry.copy(instanceFields = entry.instanceFields.map { if (it.name.id == id) it.copy(name = name) else it })
                    records[index] = entry
                }
            }
        })

        return records
    }
}
