package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class FieldValueEntity(
    var instanceId: Long,
    var fieldId: Int,
    var value: String,
    var valueInstanceId: Long = 0
) : EntityInstance {
    internal constructor() : this(0, 0, "", 0)

    companion object {
        fun anyToValueText(type: HprofValueType, any: Any) = when (type) {
            HprofValueType.Object -> (any as ULong).toString()
            HprofValueType.Char -> (any as Char).code.toString()
            else -> any.toString()
        }
    }

    override fun toString(): String = "FieldValueEntity(instanceId=$instanceId,fieldId=$fieldId,value=$value,valueInstanceId=$valueInstanceId)"
}