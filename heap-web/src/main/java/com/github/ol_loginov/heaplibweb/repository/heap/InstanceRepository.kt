package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface InstanceRepository {
    fun updateRoots(list: List<Pair<ULong, Short>>)
    fun streamAllByJavaClassId(javaClassId: Long): Stream<InstanceEntity>
    fun findById(instanceId: Long): InstanceEntity?
    fun findAllAfterId(afterId: Long, limit: Int): List<InstanceEntity>
    fun findAllRoots(): List<InstanceEntity>
    fun findAllRootInstances(): List<Long>
    fun clearRetainedSize(): Int
    fun updateRetainedSizeByClass(javaClassId: Long, retainedSize: Long): Int
}