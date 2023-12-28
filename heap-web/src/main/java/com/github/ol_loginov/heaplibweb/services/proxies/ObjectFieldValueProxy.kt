package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.ObjectFieldValue

class ObjectFieldValueProxy(
    entity: FieldValueEntity,
    scope: HeapScope
) : FieldValueProxy(entity, scope), ObjectFieldValue {
    override fun getInstance(): Instance? = scope
        .instances.findById(entity.valueInstanceId)
        ?.let { InstanceProxy(it, scope) }

    override fun getInstanceId(): Long = entity.valueInstanceId
}