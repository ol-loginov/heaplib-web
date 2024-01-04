package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.HprofStreamReader
import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.hprof.IdentifierReader
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class InstanceFieldReader(byteArray: ByteArray, override val identifierSize: Int) : AutoCloseable, HprofStreamReader {
    private val dataInput = DataInputStream(ByteArrayInputStream(byteArray))

    override val position: Long get() = throw NotImplementedError("not available here")
    override fun skip(n: Long) = dataInput.skipNBytes(n)
    override fun bytes(len: Int): ByteArray = dataInput.readNBytes(len)
    override fun close() = dataInput.close()

    fun available(): Boolean = dataInput.available() > 0

    fun primitiveText(valueType: HprofValueType): String = when (valueType) {
        HprofValueType.Float -> float()
        HprofValueType.Double -> double()
        HprofValueType.Boolean -> boolean()
        HprofValueType.Char -> char()
        HprofValueType.Byte -> byte()
        HprofValueType.Short -> short()
        HprofValueType.Int -> int()
        HprofValueType.Long -> long()
        else -> throw IllegalArgumentException("type $valueType is not primitive")
    }.toString()

    override fun id(): ULong = IdentifierReader.read(this, identifierSize)
    override fun float(): Float = dataInput.readFloat()
    override fun double(): Double = dataInput.readDouble()
    fun boolean(): Boolean = dataInput.readBoolean()
    override fun char(): Char = dataInput.readChar()
    override fun byte(): Byte = dataInput.readByte()
    override fun short(): Short = dataInput.readShort()
    override fun int(): Int = dataInput.readInt()
    override fun long(): Long = dataInput.readLong()
}