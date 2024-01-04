package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.*

class PrimitiveArrayDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_PRIM_ARRAY_DUMP) {
    private val _header = PrimitiveArrayDumpHeader()
    private val _body = PrimitiveArrayDumpBody()

    val arrayObjectId get() = _header.read(reader).arrayObjectId
    val stackTraceSN get() = _header.read(reader).stackTraceSN
    val arrayItemCount get() = _header.read(reader).arrayItemCount
    val arrayItemType get() = _header.read(reader).arrayItemType
    val arrayItems get() = _body.read(this, reader).itemSequence

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

private class SequenceGenerator<A, T>(private val array: A, private val sequencer: (A) -> Sequence<T>) : () -> Sequence<T> {
    override fun invoke(): Sequence<T> = sequencer(array)
}

private class PrimitiveArrayDumpBody {
    var ready = false
    var itemSequence: () -> Sequence<Any> = { emptySequence() }

    fun clear() {
        ready = false
        itemSequence = { emptySequence() }
    }

    fun read(view: PrimitiveArrayDumpView, reader: HprofStreamReader): PrimitiveArrayDumpBody {
        if (!ready) {
            val count = view.arrayItemCount
            val bytes = reader.bytes(count * view.arrayItemType.size)

            itemSequence = InstanceFieldReader(bytes, reader.identifierSize).use { arrayReader ->
                when (view.arrayItemType) {
                    HprofValueType.Boolean -> SequenceGenerator(BooleanArray(count) { arrayReader.ubyte() > 0U }) { it.asSequence() }
                    HprofValueType.Char -> SequenceGenerator(CharArray(count) { arrayReader.char() }) { it.asSequence() }
                    HprofValueType.Float -> SequenceGenerator(FloatArray(count) { arrayReader.float() }) { it.asSequence() }
                    HprofValueType.Double -> SequenceGenerator(DoubleArray(count) { arrayReader.double() }) { it.asSequence() }
                    HprofValueType.Byte -> SequenceGenerator(ByteArray(count) { arrayReader.byte() }) { it.asSequence() }
                    HprofValueType.Short -> SequenceGenerator(ShortArray(count) { arrayReader.short() }) { it.asSequence() }
                    HprofValueType.Int -> SequenceGenerator(IntArray(count) { arrayReader.int() }) { it.asSequence() }
                    HprofValueType.Long -> SequenceGenerator(LongArray(count) { arrayReader.long() }) { it.asSequence() }
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
