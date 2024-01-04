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
        log.info("scan classes :: visit load class records")
        val classNames = mutableMapOf<ULong, StringRef>()
        scanRecords(object : RecordVisitor {
            override fun onLoadClass(view: LoadClassRecordView) {
                classNames[view.classObjectId] = StringRef(view.nameId, null)
            }
        })

        log.info("scan classes :: visit class dumps")
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

        log.info("scan classes :: update class names")
        val records = classDumps.values.toMutableList()

        data class StringReference(val recordIndex: Int, val inName: Boolean = false, val inInstanceFields: Int = -1, val inStaticFields: Int = -1)

        val stringReferences = mutableMapOf<ULong, MutableSet<StringReference>>()

        fun addReference(stringId: ULong, reference: StringReference) {
            stringReferences.computeIfAbsent(stringId) { mutableSetOf() }.add(reference)
        }

        for ((recordIndex, v) in records.withIndex()) {
            addReference(v.className.id, StringReference(recordIndex, inName = true))
            v.instanceFields.forEachIndexed { fieldIndex, t -> addReference(t.name.id, StringReference(recordIndex, inInstanceFields = fieldIndex)) }
            v.staticFields.forEachIndexed { fieldIndex, t -> addReference(t.name.id, StringReference(recordIndex, inStaticFields = fieldIndex)) }
        }

        scanRecords(object : RecordVisitor {
            override fun onUTF8(view: UTF8RecordView) {
                val id = view.id
                val indices = stringReferences.remove(id) ?: return

                val name = StringRef(id, view.string)
                indices.forEach { ref ->
                    var entry = records[ref.recordIndex]
                    if (ref.inName) entry = entry.copy(className = name)
                    if (ref.inStaticFields >= 0) entry.staticFields[ref.inStaticFields] = entry.staticFields[ref.inStaticFields].copy(name = name)
                    if (ref.inInstanceFields >= 0) entry.instanceFields[ref.inInstanceFields] = entry.instanceFields[ref.inInstanceFields].copy(name = name)
                    records[ref.recordIndex] = entry
                }
            }
        })

        return records
    }
}
