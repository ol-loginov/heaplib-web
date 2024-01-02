package com.github.ol_loginov.heaplibweb.hprof

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.github.ol_loginov.heaplibweb.hprof.HprofTypes")

enum class RecordType(val tag: UByte) {
    UTF8(0x01u),
    LOAD_CLASS(0x02u),
    UNLOAD_CLASS(0x03u),
    FRAME(0x04u),
    TRACE(0x05u),
    ALLOC_SITES(0x06u),
    HEAP_SUMMARY(0x07u),
    START_THREAD(0x0Au),
    END_THREAD(0x0Bu),
    HEAP_DUMP(0x0Cu),
    CPU_SAMPLES(0x0Du),
    CONTROL_SETTINGS(0x0Eu),

    // 1.0.2 record types
    HEAP_DUMP_SEGMENT(0x1Cu),
    HEAP_DUMP_END(0x2Cu);

    companion object {
        const val TYPE_LIMIT = 100
        private val TYPE_NAMES = RecordType.values().map { e -> e.tag.toInt() to e.name }.toMap()
        private val TYPE_LOOKUP = RecordType.values().map { e -> e.tag to e }.toMap()

        fun typeName(type: Int): String = TYPE_NAMES[type]
            ?: "<record#${type}"

        fun valueOf(type: UByte): RecordType = TYPE_LOOKUP[type]
            ?: throw IllegalArgumentException("no record type for ${type}")
    }
}

interface RecordVisitor {
    fun onUTF8(reader: HprofStreamReader, length: Long) = skipRecord(reader, RecordType.UTF8, length)
    fun onLoadClass(reader: HprofStreamReader, length: Long) = skipRecord(reader, RecordType.LOAD_CLASS, length)
    fun onUnloadClass(reader: HprofStreamReader, length: Long) = skipRecord(reader, RecordType.UNLOAD_CLASS, length)
    fun onFrame(reader: HprofStreamReader, length: Long) = skipRecord(reader, RecordType.FRAME, length)
    fun onTrace(reader: HprofStreamReader, length: Long) = skipRecord(reader, RecordType.TRACE, length)
    fun skipRecord(reader: HprofStreamReader, type: RecordType, length: Long) = reader.skip(length)
}

enum class SubRecordType(val tag: UByte) {
    GC_ROOT_UNKNOWN(0xFFu),
    GC_ROOT_JNI_GLOBAL(0x01u),
    GC_ROOT_JNI_LOCAL(0x02u),
    GC_ROOT_JAVA_FRAME(0x03u),
    GC_ROOT_NATIVE_STACK(0x04u),
    GC_ROOT_STICKY_CLASS(0x05u),
    GC_ROOT_THREAD_BLOCK(0x06u),
    GC_ROOT_MONITOR_USED(0x07u),
    GC_ROOT_THREAD_OBJ(0x08u),
    GC_CLASS_DUMP(0x20u),
    GC_INSTANCE_DUMP(0x21u),
    GC_OBJ_ARRAY_DUMP(0x22u),
    GC_PRIM_ARRAY_DUMP(0x23u);

    companion object {
        const val TYPE_LIMIT = 100
        private val TYPE_NAMES = SubRecordType.values().map { e -> e.tag.toInt() to e.name }.toMap()
        private val TYPE_LOOKUP = SubRecordType.values().map { e -> e.tag to e }.toMap()

        fun typeName(type: Int): String = TYPE_NAMES[type]
            ?: "<sub-type#${type}"

        fun valueOf(type: UByte): SubRecordType = TYPE_LOOKUP[type]
            ?: throw IllegalArgumentException("no sub record type for ${type}")
    }
}


interface HeapRootVisitor {
    fun onRootUnknown(objectId: ULong) {}
    fun onRootJniGlobal(objectId: ULong, jniGlobalRefId: ULong) {}
    fun onRootJniLocal(objectId: ULong, threadSN: UInt, frame: UInt) {}
    fun onRootThreadObject(objectId: ULong, threadSN: UInt, stackTraceSN: UInt) {}
    fun onRootJavaFrame(objectId: ULong, threadSN: UInt, frame: UInt) {}
    fun onRootStickyClass(objectId: ULong) {}
    fun onRootNativeStack(objectId: ULong, threadSN: UInt) {}
    fun onRootThreadBlock(objectId: ULong, threadSN: UInt) {}
    fun onRootMonitorUsed(objectId: ULong) {}
}

interface HeapDumpVisitor : HeapRootVisitor {
    fun onGCClassDump(dump: ClassDump) {}
    fun onGCInstanceDump(dump: InstanceDump) {}
    fun onGCPrimitiveArrayDump(dump: PrimitiveArrayDump) {}
    fun onGCObjectArrayDump(dump: ObjectArrayDump) {}
}

enum class HprofValueType(val tag: UByte, val size: kotlin.Byte) {
    Array(0x01u, -1),
    Object(0x02u, -1),
    Boolean(0x04u, 1),
    Char(0x05u, 1),
    Float(0x06u, 4),
    Double(0x07u, 8),
    Byte(0x08u, 1),
    Short(0x09u, 2),
    Int(0x0Au, 4),
    Long(0x0Bu, 8);

    companion object {
        private val TYPE_NAMES = HprofValueType.values().map { e -> e.tag.toInt() to e.name }.toMap()
        private val TYPE_LOOKUP = HprofValueType.values().map { e -> e.tag to e }.toMap()

        fun valueOf(tag: UByte): HprofValueType = TYPE_LOOKUP[tag]
            ?: throw IllegalArgumentException("no type for tag ${tag}")
    }
}


data class ValueRecord(val type: HprofValueType, val content: Any) {
    companion object {
        fun boolean(ubyte: UByte) = ValueRecord(HprofValueType.Boolean, ubyte != 0.toUByte())
        fun byte(byte: Byte) = ValueRecord(HprofValueType.Boolean, byte)
        fun obj(id: ULong) = ValueRecord(HprofValueType.Object, id)
        fun char(ushort: UShort) = ValueRecord(HprofValueType.Object, Char(ushort))
        fun float(float: Float) = ValueRecord(HprofValueType.Float, float)
        fun double(double: Double) = ValueRecord(HprofValueType.Double, double)
        fun short(short: Short) = ValueRecord(HprofValueType.Short, short)
        fun int(int: Int) = ValueRecord(HprofValueType.Int, int)
        fun long(long: Long) = ValueRecord(HprofValueType.Long, long)
    }
}

data class StringRef(val id: ULong = 0u, val name: String? = null) {
    fun orEmpty() = name ?: ""
}

data class NamedType(val name: StringRef, val type: HprofValueType)
data class NamedValue(val name: StringRef, val type: HprofValueType, val value: Any)

data class ClassDump(
    val classObjectId: ULong,
    val starkTraceSN: UInt,
    val superClassObjectId: ULong,
    val classLoaderObjectId: ULong,
    val signersObjectId: ULong,
    val domainObjectId: ULong,
    val instanceSize: UInt,

    val className: StringRef,

    val constantPool: List<ValueRecord>,
    val staticFields: List<NamedValue>,
    val instanceFields: List<NamedType>,
)

data class PrimitiveArrayDump(val objectId: ULong, val starkTraceSN: UInt, val type: HprofValueType, val array: List<Any>)
data class ObjectArrayDump(val objectId: ULong, val starkTraceSN: UInt, val arrayClassId: ULong, val array: List<ULong>)
data class InstanceDump(val objectId: ULong, val starkTraceSN: UInt, val classObjectId: ULong, val fieldsData: ByteArray) {
    fun hasFieldData(): Boolean = fieldsData.isNotEmpty()
}
