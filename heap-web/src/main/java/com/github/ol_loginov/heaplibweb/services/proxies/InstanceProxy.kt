package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.Value

class InstanceProxy(
    private val entity: InstanceEntity,
    private val scope: HeapScope
) : Instance {
    companion object {
        @JvmStatic
        fun wrap(entity: InstanceEntity, scope: HeapScope): Instance = InstanceProxy(entity, scope)
    }

    override fun getFieldValues(): List<FieldValue> = scope
        .fieldValues.streamInstanceFieldValues(entity.instanceId)
        .map { FieldValueProxy.wrap(it, scope) }
        .toList()

    override fun isGCRoot(): Boolean = entity.rootTag > 0
    override fun getInstanceId(): Long = entity.instanceId
    override fun getInstanceNumber(): Int = entity.instanceNumber

    override fun getJavaClass(): JavaClass? = scope
        .javaClasses.findById(entity.javaClassId)
        ?.let { JavaClassProxy(it, scope) }

    override fun getSize(): Long = entity.size
    override fun getReachableSize(): Long = entity.reachableSize ?: throw UnsupportedOperationException("number not ready")
    override fun getRetainedSize(): Long = entity.retainedSize ?: throw UnsupportedOperationException("number not ready")

    override fun getValueOfField(name: String): Any? {
        val fieldValueEntity = scope.fieldValues
            .findOneByInstanceAndFieldName(entity.instanceId, name)
            ?: return null
        return FieldValueProxy.getValueObject(fieldValueEntity, scope)
    }

    override fun getNearestGCRootPointer(): Instance = throw UnsupportedOperationException()
    override fun getReferences(): List<Value> = throw UnsupportedOperationException()
    override fun getStaticFieldValues(): List<FieldValue> = throw UnsupportedOperationException()

    override fun toString(): String = "${this::class.java.simpleName}#${entity.instanceId}"
}