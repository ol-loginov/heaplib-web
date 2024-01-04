package com.github.ol_loginov.heaplibweb.hprof

import com.github.ol_loginov.heaplibweb.support.void
import java.io.ByteArrayInputStream
import java.io.DataInputStream

interface HprofStreamReader {
    val position: Long
    val identifierSize: Int

    fun skip(n: Int) = skip(n.toLong())
    fun skip(n: Long)
    fun uint(): UInt = int().toUInt()
    fun int(): Int
    fun ulong(): ULong = long().toULong()
    fun long(): Long
    fun ubyte(): UByte = byte().toUByte()
    fun byte(): Byte
    fun ushort(): UShort = short().toUShort()
    fun short(): Short
    fun char(): Char
    fun float(): Float
    fun double(): Double
    fun id(): ULong
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

    fun string(bytes: Long, noTerminalZero: Boolean = true): String = string(bytes.toInt(), noTerminalZero)
    fun string(bytes: Int, noTerminalZero: Boolean = true): String {
        val textBytes = if (noTerminalZero) bytes else bytes - 1
        val restBytes = if (noTerminalZero) 0L else 1L
        val text = bytes(textBytes).toString(Charsets.UTF_8)
        skip(restBytes)
        return text
    }

    fun bytes(len: Int): ByteArray
}

object IdentifierReader {
    fun read(delegate: HprofStreamReader, identifierSize: Int) = when (identifierSize) {
        4 -> delegate.int().toULong()
        8 -> delegate.ulong()
        else -> throw NotImplementedError("invalid identifier size - $identifierSize")
    }
}

class HprofFileReader(val stream: HprofFileSource, override val identifierSize: Int, initialPosition: Long) : HprofStreamReader {
    override var position = initialPosition
        private set

    fun available() = stream.available() > 0

    private fun forward(length: Long) {
        position += length
    }

    override fun skip(n: Long) {
        if (n == 0L) return
        forward(n)
        stream.skipNBytes(n)
    }

    override fun int(): Int {
        forward(4)
        return stream.readInt()
    }

    override fun long(): Long {
        forward(8)
        return stream.readLong()
    }

    override fun byte(): Byte {
        forward(1)
        return stream.readByte()
    }

    override fun short(): Short {
        forward(2)
        return stream.readShort()
    }

    override fun char(): Char = Char(ushort())

    override fun float(): Float {
        forward(4)
        return stream.readFloat()
    }

    override fun double(): Double {
        forward(8)
        return stream.readDouble()
    }

    override fun id(): ULong = IdentifierReader.read(this, identifierSize)

    override fun bytes(len: Int): ByteArray {
        forward(len.toLong())
        return stream.readNBytes(len)
    }
}

class HprofRecordReader(private val delegate: HprofFileReader) : HprofStreamReader {
    override val identifierSize = delegate.identifierSize
    val start = delegate.position
    val end get() = start + 1 + 4 + 4 + length

    @Suppress("JoinDeclarationAndAssignment")
    val recordType: RecordType
    val length: Long

    init {
        recordType = RecordType.byTag(delegate.ubyte())
        @Suppress("UNUSED_VARIABLE")
        val recordTimeOffset = delegate.uint()
        length = delegate.uint().toLong()
    }

    override val position: Long
        get() = delegate.position

    fun available(): Boolean = delegate.position < start + length

    private fun checkForward(bytes: Long) {
        if (delegate.position + bytes > end) {
            throw IllegalStateException("read too much")
        }
    }

    override fun skip(n: Long) {
        checkForward(n)
        delegate.skip(n)
    }

    override fun int(): Int {
        checkForward(4)
        return delegate.int()
    }

    override fun long(): Long {
        checkForward(8)
        return delegate.long()
    }

    override fun byte(): Byte {
        checkForward(1)
        return delegate.byte()
    }

    override fun short(): Short {
        checkForward(2)
        return delegate.short()
    }

    override fun char(): Char {
        checkForward(2)
        return delegate.char()
    }

    override fun float(): Float {
        checkForward(4)
        return delegate.float()
    }

    override fun double(): Double {
        checkForward(8)
        return delegate.double()
    }

    override fun id(): ULong {
        checkForward(identifierSize.toLong())
        return delegate.id()
    }

    override fun string(bytes: Int, noTerminalZero: Boolean): String {
        checkForward(bytes.toLong())
        return delegate.string(bytes, noTerminalZero)
    }

    override fun bytes(len: Int): ByteArray {
        checkForward(len.toLong())
        return delegate.bytes(len)
    }

    fun skipToEnd() {
        skip(end - delegate.position)
    }
}

class HprofByteArrayReader(bytes: ByteArray, override val identifierSize: Int) : HprofStreamReader {
    private val dis = DataInputStream(ByteArrayInputStream(bytes))

    override val position: Long
        get() = TODO("Not yet implemented")

    override fun skip(n: Long) = dis.skip(n).void()
    override fun int(): Int = dis.readInt()
    override fun long(): Long = dis.readLong()
    override fun byte(): Byte = dis.readByte()
    override fun short(): Short = dis.readShort()
    override fun char(): Char = dis.readChar()
    override fun float(): Float = dis.readFloat()
    override fun double(): Double = dis.readDouble()
    override fun id(): ULong = IdentifierReader.read(this, identifierSize)
    override fun bytes(len: Int): ByteArray = dis.readNBytes(len)
}