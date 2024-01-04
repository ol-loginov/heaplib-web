package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class InstanceFieldReader(byteArray: ByteArray) : AutoCloseable {
    private val dataInputStream: Lazy<DataInputStream> = lazy { DataInputStream(ByteArrayInputStream(byteArray)) }

    override fun close() {
        if (dataInputStream.isInitialized()) {
            dataInputStream.value.close()
        }
    }

    fun available(): Boolean = dataInputStream.value.available() > 0

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

    fun ulong(): ULong = dataInputStream.value.readLong().toULong()
    fun float(): Float = dataInputStream.value.readFloat()
    fun double(): Double = dataInputStream.value.readDouble()
    fun boolean(): Boolean = dataInputStream.value.readBoolean()
    fun char(): Char = dataInputStream.value.readChar()
    fun byte(): Byte = dataInputStream.value.readByte()
    fun short(): Short = dataInputStream.value.readShort()
    fun int(): Int = dataInputStream.value.readInt()
    fun long(): Long = dataInputStream.value.readLong()
}