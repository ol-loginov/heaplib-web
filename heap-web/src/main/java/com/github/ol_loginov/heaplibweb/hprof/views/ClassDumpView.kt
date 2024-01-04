package com.github.ol_loginov.heaplibweb.hprof.views

import com.github.ol_loginov.heaplibweb.hprof.*

class ClassDumpView(reader: HprofStreamReader) : DumpView(reader, SubRecordType.GC_CLASS_DUMP) {
    private val _header = ClassDumpHeaderSection()
    val classObjectId get() = _header.read(reader).classObjectId
    val stackTraceSN get() = _header.read(reader).stackTraceSN
    val superClassObjectId get() = _header.read(reader).superClassObjectId
    val classLoaderObjectId get() = _header.read(reader).classLoaderObjectId
    val signersObjectId get() = _header.read(reader).signersObjectId
    val domainObjectId get() = _header.read(reader).domainObjectId
    val instanceSize get() = _header.read(reader).instanceSize

    private val _constantPool = ClassDumpConstantPoolSection()
    val constantPool: Array<ValueRecord>
        get() {
            _header.read(reader)
            return _constantPool.read(reader).items.toTypedArray()
        }

    private val _staticFields = ClassDumpStaticFieldsSection()
    val staticFields: Array<NamedValue>
        get() {
            _header.read(reader)
            _constantPool.read(reader)
            return _staticFields.read(reader).items.toTypedArray()
        }

    private val _instanceFields = ClassDumpInstanceFieldsSection()
    val instanceFields: Array<NamedType>
        get() {
            _header.read(reader)
            _constantPool.read(reader)
            _staticFields.read(reader)
            return _instanceFields.read(reader).items.toTypedArray()
        }

    override fun skip() {
        if (!_header.ready) reader.skip(_header.length(reader).toLong())
        _constantPool.read(reader)
        _staticFields.read(reader)
        _instanceFields.read(reader)
    }

    override fun reset() {
        _header.clear()
        _constantPool.clear()
        _staticFields.clear()
        _instanceFields.clear()
    }
}


private class ClassDumpHeaderSection {
    var ready = false
    var classObjectId: ULong = 0UL
    var stackTraceSN: UInt = 0U
    var superClassObjectId: ULong = 0UL
    var classLoaderObjectId: ULong = 0UL
    var signersObjectId: ULong = 0UL
    var domainObjectId: ULong = 0UL
    var instanceSize: UInt = 0U

    fun clear() {
        ready = false
    }

    fun length(reader: HprofStreamReader): Int = reader.identifierSize * 7 + 4 * 2
    fun read(reader: HprofStreamReader): ClassDumpHeaderSection {
        if (!ready) {
            val data = HprofByteArrayReader(reader.bytes(length(reader)), reader.identifierSize)
            classObjectId = data.id()
            stackTraceSN = data.uint()
            superClassObjectId = data.id()
            classLoaderObjectId = data.id()
            signersObjectId = data.id()
            domainObjectId = data.id()
            data.id()
            data.id()
            instanceSize = data.uint()
            ready = true
        }
        return this
    }
}

private fun readValueRecord(reader: HprofStreamReader): ValueRecord {
    return when (val type = reader.type()) {
        HprofValueType.Boolean -> ValueRecord.boolean(reader.ubyte())
        HprofValueType.Byte -> ValueRecord.byte(reader.byte())
        HprofValueType.Object -> ValueRecord.obj(reader.id())
        HprofValueType.Char -> ValueRecord.char(reader.ushort())
        HprofValueType.Float -> ValueRecord.float(reader.float())
        HprofValueType.Double -> ValueRecord.double(reader.double())
        HprofValueType.Short -> ValueRecord.short(reader.short())
        HprofValueType.Int -> ValueRecord.int(reader.int())
        HprofValueType.Long -> ValueRecord.long(reader.long())
        else -> throw NotImplementedError("type $type is not supported here")
    }
}

private class ClassDumpConstantPoolSection {
    var ready = false
    val items = mutableListOf<ValueRecord>()

    fun clear() {
        ready = false
        items.clear()
    }

    fun read(reader: HprofStreamReader): ClassDumpConstantPoolSection {
        if (!ready) {
            val count = reader.ushort().toInt()
            for (constantPoolIt in 0 until count) {
                reader.ushort()
                items.add(readValueRecord(reader))
            }
            ready = true
        }
        return this
    }
}

private class ClassDumpStaticFieldsSection {
    var ready = false
    val items = mutableListOf<NamedValue>()

    fun clear() {
        ready = false
        items.clear()
    }

    fun read(reader: HprofStreamReader): ClassDumpStaticFieldsSection {
        if (!ready) {
            for (i in 0 until reader.ushort().toInt()) {
                val name = reader.id()
                val value = readValueRecord(reader)
                items.add(NamedValue(StringRef(name), value.type, value.content))
            }
            ready = true
        }
        return this
    }
}

private class ClassDumpInstanceFieldsSection {
    var ready = false
    val items = mutableListOf<NamedType>()

    fun clear() {
        ready = false
        items.clear()
    }

    fun read(reader: HprofStreamReader): ClassDumpInstanceFieldsSection {
        if (!ready) {
            for (i in 0 until reader.ushort().toInt()) {
                val name = reader.id()
                items.add(NamedType(StringRef(name), reader.type()))
            }
            ready = true
        }
        return this
    }
}
