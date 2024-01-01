package com.github.ol_loginov.heaplibweb.hprof

import java.io.DataInputStream
import java.util.function.Supplier

class HprofStreamReader(private val stream: DataInputStream, val idLength: Int, private val limit: Long? = null) {
    fun limited(limit: Long): HprofStreamReader = HprofStreamReader(stream, idLength, limit)

    private val idReader = when (idLength) {
        4 -> Supplier<ULong> { uint().toULong() }
        8 -> Supplier<ULong> { ulong() }
        else -> throw NotImplementedError("invalid size of identifiers ($idLength)")
    }

    var bytesRead = 0L

    fun available() = if (limit != null) bytesRead < limit else stream.available() > 0

    private fun incrementRead(length: Long) {
        bytesRead += length
        if (limit != null && bytesRead > limit) {
            throw IllegalStateException("read too much (limit=$limit, read=$bytesRead")
        }
    }

    fun skip(length: Long) {
        var rest = length
        while (rest > 0) {
            val skipped = stream.skip(rest)
            incrementRead(skipped)
            rest -= skipped
        }
    }

    fun uint() = int().toUInt()
    fun int(): Int {
        incrementRead(4)
        return stream.readInt()
    }

    fun ulong(): ULong = long().toULong()
    fun long(): Long {
        incrementRead(8)
        return stream.readLong()
    }

    fun ubyte(): UByte = byte().toUByte()
    fun byte(): Byte {
        incrementRead(1)
        return stream.readByte()
    }

    fun ushort(): UShort = short().toUShort()
    fun short(): Short {
        incrementRead(2)
        return stream.readShort()
    }

    fun char(): Char = Char(ushort())

    fun float(): Float {
        incrementRead(4)
        return stream.readFloat()
    }

    fun double(): Double {
        incrementRead(8)
        return stream.readDouble()
    }

    fun id(): ULong {
        return idReader.get()
    }

    fun type(): HprofValueType = when (val type = ubyte()) {
        HprofValueType.Boolean.tag -> HprofValueType.Boolean
        HprofValueType.Byte.tag -> HprofValueType.Byte
        HprofValueType.Object.tag -> HprofValueType.Object
        HprofValueType.Char.tag -> HprofValueType.Char
        HprofValueType.Float.tag -> HprofValueType.Float
        HprofValueType.Double.tag -> HprofValueType.Double
        HprofValueType.Short.tag -> HprofValueType.Short
        HprofValueType.Int.tag -> HprofValueType.Int
        HprofValueType.Long.tag -> HprofValueType.Long
        else -> throw NotImplementedError("type $type is unknown")
    }

    fun string(length: Long, noTerminalZero: Boolean = true): String {
        val textBytes = if (noTerminalZero) length.toInt() else length.toInt() - 1
        val restBytes = if (noTerminalZero) 0L else 1L

        incrementRead(textBytes.toLong())
        val text = stream.readNBytes(textBytes).toString(Charsets.UTF_8)
        skip(restBytes)
        return text
    }

    fun bytes(length: Int): ByteArray {
        incrementRead(length.toLong())
        return stream.readNBytes(length)
    }
}