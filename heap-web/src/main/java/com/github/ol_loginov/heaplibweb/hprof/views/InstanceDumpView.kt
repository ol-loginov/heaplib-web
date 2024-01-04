package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.DumpView
import com.github.ol_loginov.heaplibweb.hprof.HprofStreamReader
import com.github.ol_loginov.heaplibweb.hprof.LazyFixedData
import com.github.ol_loginov.heaplibweb.hprof.SubRecordType
import com.github.ol_loginov.heaplibweb.support.LazyReset
import com.github.ol_loginov.heaplibweb.support.tuple

class InstanceDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_INSTANCE_DUMP) {
    private val _header = LazyFixedData(reader.identifierSize + 4 + 4) { tuple(reader.id(), reader.uint(), reader.id(), reader.int()) }
    private val _body = LazyReset { reader.bytes(bodyLength) }

    override fun reset() = clearAll(_header, _body)
    override fun skip() = skip(bodyLength, _body.isInitialized())

    val objectId get() = _header.value.first
    val threadSN get() = _header.value.second
    val classObjectId get() = _header.value.third
    private val bodyLength get() = _header.value.fourth

    fun hasFieldData(): Boolean = bodyLength > 0
    fun getFieldReader() = InstanceFieldReader(_body.value, reader.identifierSize)
}
