package com.github.ol_loginov.heaplibweb.hprof

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(ClassDumpScanner::class.java)

internal class ClassDumpScanner(
    private val hprofStream: HprofStream
) {
    fun scan(): List<ClassDump> {
        log.info("scan LoadClassRecords")
        val classNamesMap = mutableMapOf<ULong, StringRef>()
        val recordMap = mutableMapOf<ULong, ClassDump>()
        val recordCollector = RecordCollector(classNamesMap, recordMap)
        hprofStream.scan(recordCollector, recordCollector)

        log.info("name LoadClassRecords")
        val records = recordMap.values.toMutableList()
        hprofStream.scan(RecordNamer(records), null)

        return records
    }
}

private class RecordCollector(
    private val classNames: MutableMap<ULong, StringRef>,
    private val records: MutableMap<ULong, ClassDump>
) : RecordVisitor, HeapDumpVisitor {
    @Suppress("UNUSED_VARIABLE")
    override fun onLoadClass(reader: HprofStreamReader, length: Long) {
        val classSN = reader.uint()
        val classObjectId = reader.id()
        val stackTraceSN = reader.uint()
        val nameId = reader.id()
        classNames[classObjectId] = StringRef(nameId, null)
    }

    override fun onGCClassDump(dump: ClassDump) {
        records[dump.classObjectId] = dump.copy(className = classNames[dump.classObjectId] ?: StringRef())
    }
}

private class RecordNamer(private val records: MutableList<ClassDump>) : RecordVisitor {
    private val updateEntryReferences = mutableMapOf<ULong, MutableSet<Int>>()

    init {
        fun addReference(stringId: ULong, entityIndex: Int) {
            updateEntryReferences.computeIfAbsent(stringId) { mutableSetOf() }.add(entityIndex)
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
    }

    override fun onUTF8(reader: HprofStreamReader, length: Long) {
        val id = reader.id()
        val rest = length - reader.idLength

        val indices = updateEntryReferences.remove(id)
        if (indices == null) {
            reader.skip(rest)
            return
        }

        val name = StringRef(id, reader.string(rest))
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
}