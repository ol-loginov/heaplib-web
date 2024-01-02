package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface InstanceRepository {
    fun persist(entity: InstanceEntity)
    fun persistAll(entities: List<InstanceEntity>)

    fun updateRoots(list: List<Pair<ULong, Short>>)

    fun streamAllByJavaClassId(javaClassId: Long): Stream<InstanceEntity>

    fun findById(instanceId: Long): InstanceEntity?
}