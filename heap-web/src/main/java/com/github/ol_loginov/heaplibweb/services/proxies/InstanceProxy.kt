package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.Value

class InstanceProxy(
    private val entity: InstanceEntity,
    private val heapRepositories: HeapRepositories
) : Instance {
    companion object {
        @JvmStatic
        fun wrap(entity: InstanceEntity, heapRepositories: HeapRepositories): Instance = InstanceProxy(entity, heapRepositories)
    }

    override fun getFieldValues(): List<FieldValue> = heapRepositories
        .fieldValues.streamInstanceFieldValues(entity.instanceId)
        .map { FieldValueProxy.wrap(it, heapRepositories) }
        .toList()

    override fun isGCRoot(): Boolean = entity.rootTag > 0
    override fun getInstanceId(): Long = entity.instanceId
    override fun getInstanceNumber(): Int = entity.instanceNumber

    override fun getJavaClass(): JavaClass? = heapRepositories
        .classes.findById(entity.javaClassId)
        ?.let { JavaClassProxy(it, heapRepositories) }

    override fun getSize(): Long = entity.size.toLong()
    override fun getReachableSize(): Long = entity.reachableSize ?: throw UnsupportedOperationException("number not ready")
    override fun getRetainedSize(): Long = entity.retainedSize ?: throw UnsupportedOperationException("number not ready")

    override fun getValueOfField(name: String): Any? {
        val fieldValueEntity = heapRepositories.fieldValues
            .findOneByInstanceAndFieldName(entity.instanceId, name)
            ?: return null
        return FieldValueProxy.getValueObject(fieldValueEntity, heapRepositories)
    }

    override fun getNearestGCRootPointer(): Instance = throw UnsupportedOperationException()
    override fun getReferences(): List<Value> = throw UnsupportedOperationException()
    override fun getStaticFieldValues(): List<FieldValue> = throw UnsupportedOperationException()

    override fun toString(): String = "${this::class.java.simpleName}#${entity.instanceId}"
}
