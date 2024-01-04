package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class PrimitiveArrayDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_PRIM_ARRAY_DUMP) {
    private val _header = PrimitiveArrayDumpHeader()
    private val _body = PrimitiveArrayDumpBody()

    val arrayObjectId get() = _header.read(reader).arrayObjectId
    val stackTraceSN get() = _header.read(reader).stackTraceSN
    val arrayItemCount get() = _header.read(reader).arrayItemCount
    val arrayItemType get() = _header.read(reader).arrayItemType
    val arrayItems get() = _body.read(this, reader).items

    override fun skip() {
        _body.skip(this, reader)
    }

    override fun reset() {
        _header.clear()
        _body.clear()
    }
}


private class PrimitiveArrayDumpHeader {
    var ready = false
    var arrayObjectId: ULong = 0UL
    var stackTraceSN: UInt = 0U
    var arrayItemCount: Int = 0
    var arrayItemType: HprofValueType = HprofValueType.Object

    fun clear() {
        ready = false
    }

    fun length(reader: HprofStreamReader): Int = reader.identifierSize + 4 * 2 + 1
    fun read(reader: HprofStreamReader): PrimitiveArrayDumpHeader {
        if (!ready) {
            val data = HprofByteArrayReader(reader.bytes(length(reader)), reader.identifierSize)
            arrayObjectId = data.id()
            stackTraceSN = data.uint()
            arrayItemCount = data.int()
            arrayItemType = data.type()
            ready = true
        }
        return this
    }
}

private class PrimitiveArrayDumpBody {
    var ready = false
    var items = listOf<Any>()

    fun clear() {
        ready = false
    }

    fun read(view: PrimitiveArrayDumpView, reader: HprofStreamReader): PrimitiveArrayDumpBody {
        if (!ready) {
            val count = view.arrayItemCount.toInt()
            val bytes = reader.bytes(count * view.arrayItemType.size)

            items = DataInputStream(ByteArrayInputStream(bytes)).use { data ->
                when (view.arrayItemType) {
                    HprofValueType.Boolean -> IntRange(1, count).map { reader.ubyte() }
                    HprofValueType.Char -> IntRange(1, count).map { reader.char() }
                    HprofValueType.Float -> IntRange(1, count).map { reader.float() }
                    HprofValueType.Double -> IntRange(1, count).map { reader.double() }
                    HprofValueType.Byte -> IntRange(1, count).map { reader.byte() }
                    HprofValueType.Short -> IntRange(1, count).map { reader.short() }
                    HprofValueType.Int -> IntRange(1, count).map { reader.int() }
                    HprofValueType.Long -> IntRange(1, count).map { reader.long() }
                    else -> throw NotImplementedError("Expect primitive type of array, got ${view.arrayItemType}")
                }
            }

            ready = true
        }
        return this
    }

    fun skip(view: PrimitiveArrayDumpView, reader: HprofStreamReader) {
        if (ready) return
        reader.skip(view.arrayItemCount * view.arrayItemType.size)
    }
}
