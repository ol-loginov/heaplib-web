package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.springframework.dao.TransientDataAccessResourceException

open class FieldValueProxy(
    protected val entity: FieldValueEntity,
    protected val scope: HeapScope
) : FieldValue {
    companion object {
        @JvmStatic
        fun wrap(entity: FieldValueEntity, scope: HeapScope): FieldValue {
            return if (entity.valueInstanceId == null) FieldValueProxy(entity, scope) else ObjectFieldValueProxy(entity, scope)
        }

        @JvmStatic
        fun getValueObject(fieldValue: FieldValueEntity, scope: HeapScope): Any? {
            val fieldEntity = scope.fields.findById(fieldValue.fieldId) ?: throw TransientDataAccessResourceException("no Field#${fieldValue.fieldId}")
            val typeEntity = scope.types.findById(fieldEntity.typeId) ?: throw TransientDataAccessResourceException("no Type#${fieldEntity.typeId}")
            return when (typeEntity.name) {
                "object" -> fieldValue.valueInstanceId
                    ?.let { scope.instances.findById(it) }
                    ?.let { InstanceProxy.wrap(it, scope) }

                "boolean" -> "0" != fieldValue.value
                "byte" -> fieldValue.value.toByte()
                "short" -> fieldValue.value.toShort()
                "int" -> fieldValue.value.toInt()
                "long" -> fieldValue.value.toLong()
                "char" -> fieldValue.value[0]
                "float" -> fieldValue.value.toFloat()
                "double" -> fieldValue.value.toDouble()
                else -> throw IllegalStateException("${typeEntity.name} is not supported")
            }
        }
    }


    override fun getField(): Field {
        val field = scope.fields.findById(entity.fieldId) ?: throw TransientDataAccessResourceException("no Field#${entity.fieldId}")
        return FieldProxy(field, scope)
    }

    override fun getValue(): String = entity.value

    override fun getDefiningInstance(): Instance? {
        return scope.instances
            .findById(entity.definingInstanceId)
            ?.let { InstanceProxy(it, scope) }
    }
}