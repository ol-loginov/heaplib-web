package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofValueType

class ClassDumpLookup {
    private val classDumps: MutableMap<ULong, ClassDump> = mutableMapOf()

    val classes: Collection<ClassDump>
        get() = classDumps.values
    val classCount: Int
        get() = classDumps.size

    var primitiveArrays: MutableMap<HprofValueType, ClassDump> = mutableMapOf()

    fun putAll(scanClasses: List<ClassDump>) {
        classDumps.putAll(scanClasses.associateBy { it.classObjectId })

        val valueTypeMap = mapOf(
            "[Z" to HprofValueType.Boolean, "boolean[]" to HprofValueType.Boolean,
            "[C" to HprofValueType.Char, "char[]" to HprofValueType.Char,
            "[F" to HprofValueType.Float, "float[]" to HprofValueType.Float,
            "[D" to HprofValueType.Double, "double[]" to HprofValueType.Double,
            "[B" to HprofValueType.Byte, "byte[]" to HprofValueType.Byte,
            "[S" to HprofValueType.Short, "short[]" to HprofValueType.Short,
            "[I" to HprofValueType.Int, "int[]" to HprofValueType.Int,
            "[J" to HprofValueType.Long, "long[]" to HprofValueType.Long,
        )
        classDumps.values.forEach { dump ->
            valueTypeMap[dump.className.orEmpty()]?.let {
                primitiveArrays.put(it, dump)
            }
        }
    }

    fun lookupPrimitiveArrayClassId(type: HprofValueType): ULong {
        val classDump = primitiveArrays[type] ?: throw IllegalArgumentException("no primitive array class for type $type")
        return classDump.classObjectId
    }

    fun lookup(classObjectId: ULong): ClassDump {
        return classDumps[classObjectId] ?: throw IllegalArgumentException("no class dump for ($classObjectId)")
    }
}