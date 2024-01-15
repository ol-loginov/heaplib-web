package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.ObjectFieldValue

class ObjectFieldValueProxy(
    entity: FieldValueEntity,
    heapRepositories: HeapRepositories
) : FieldValueProxy(entity, heapRepositories), ObjectFieldValue {
    override fun getInstance(): Instance? = heapRepositories
        .instances.findById(entity.valueInstanceId)
        ?.let { InstanceProxy(it, heapRepositories) }

    override fun getInstanceId(): Long = entity.valueInstanceId
}