package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.DumpView
import com.github.ol_loginov.heaplibweb.hprof.HprofByteArrayReader
import com.github.ol_loginov.heaplibweb.hprof.HprofStreamReader
import com.github.ol_loginov.heaplibweb.hprof.SubRecordType
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class ObjectArrayDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_OBJ_ARRAY_DUMP) {
    private val _header = ObjectArrayDumpHeader()
    private val _body = ObjectArrayDumpBody()

    val objectId get() = _header.read(reader).objectId
    val stackTraceSN get() = _header.read(reader).stackTraceSN
    val arraySize get() = _header.read(reader).arraySize
    val classId get() = _header.read(reader).classId
    val items get() = _body.read(this, reader).items

    override fun skip() {
        _body.skip(this, reader)
    }

    override fun reset() {
        _header.clear()
        _body.clear()
    }
}

private class ObjectArrayDumpHeader {
    var ready = false
    var objectId: ULong = 0UL
    var stackTraceSN: UInt = 0U
    var arraySize: Int = 0
    var classId: ULong = 0UL

    fun clear() {
        ready = false
    }

    fun length(reader: HprofStreamReader): Int = reader.identifierSize * 2 + 4 * 2
    fun read(reader: HprofStreamReader): ObjectArrayDumpHeader {
        if (!ready) {
            val data = HprofByteArrayReader(reader.bytes(length(reader)), reader.identifierSize)
            objectId = data.id()
            stackTraceSN = data.uint()
            arraySize = data.int()
            classId = data.id()
            ready = true
        }
        return this
    }
}

private class ObjectArrayDumpBody {
    var ready = false
    var items = listOf<ULong>()

    fun clear() {
        ready = false
        items = emptyList()
    }

    fun read(view: ObjectArrayDumpView, reader: HprofStreamReader): ObjectArrayDumpBody {
        if (!ready) {
            val count = view.arraySize
            val bytes = reader.bytes(count * reader.identifierSize)

            items = DataInputStream(ByteArrayInputStream(bytes)).use { data ->
                IntRange(1, count).map { data.readLong().toULong() }
            }

            ready = true
        }
        return this
    }

    fun skip(view: ObjectArrayDumpView, reader: HprofStreamReader) {
        if (ready) return
        reader.skip(view.arraySize * reader.identifierSize)
    }
}
