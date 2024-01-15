package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.springframework.dao.TransientDataAccessResourceException

open class FieldValueProxy(
    protected val entity: FieldValueEntity,
    protected val heapRepositories: HeapRepositories
) : FieldValue {
    companion object {
        @JvmStatic
        fun wrap(entity: FieldValueEntity, heapRepositories: HeapRepositories): FieldValue {
            TODO()
//            return if (entity.valueInstanceId > 0) ObjectFieldValueProxy(entity, scope) else FieldValueProxy(entity, scope)
        }

        @JvmStatic
        fun getValueObject(fieldValue: FieldValueEntity, heapRepositories: HeapRepositories): Any? {
            TODO()
            /*            
                        val fieldEntity = heapRepositories.fields.findById(fieldValue.fieldId) ?: throw TransientDataAccessResourceException("no Field#${fieldValue.fieldId}")
                        return when (val type = HprofValueType.valueOf(fieldEntity.typeTag.toUByte())) {
                            HprofValueType.Object -> heapRepositories
                                .instances.findById(fieldValue.valueInstanceId)
                                ?.let { InstanceProxy.wrap(it, heapRepositories) }
            
                            HprofValueType.Boolean -> "0" != fieldValue.value
                            HprofValueType.Byte -> fieldValue.value.toByte()
                            HprofValueType.Short -> fieldValue.value.toShort()
                            HprofValueType.Int -> fieldValue.value.toInt()
                            HprofValueType.Long -> fieldValue.value.toLong()
                            HprofValueType.Char -> fieldValue.value[0]
                            HprofValueType.Float -> fieldValue.value.toFloat()
                            HprofValueType.Double -> fieldValue.value.toDouble()
                            else -> throw IllegalStateException("$type is not supported")
                        }
                        
             */
        }
    }


    override fun getField(): Field {
        val field = heapRepositories.fields.findById(entity.fieldId) ?: throw TransientDataAccessResourceException("no Field#${entity.fieldId}")
        return FieldProxy(field, heapRepositories)
    }

    override fun getValue(): String = TODO()

    override fun getDefiningInstance(): Instance? {
        return heapRepositories.instances
            .findById(entity.instanceId)
            ?.let { InstanceProxy(it, heapRepositories) }
    }
}
