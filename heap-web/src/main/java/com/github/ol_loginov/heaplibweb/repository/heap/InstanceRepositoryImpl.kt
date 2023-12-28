package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

internal class InstanceRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : InstanceRepository {
    override fun persist(entity: InstanceEntity) {
        jdbc
            .sql(
                """
                insert into Instance(instanceId, instanceNumber, javaClassId, gcRoot, size, retainedSize, reachableSize)
                values(:instanceId, :instanceNumber, :javaClassId, :gcRoot, :size, :retainedSize, :reachableSize)
            """
            )
            .param("instanceId", entity.instanceId)
            .param("instanceNumber", entity.instanceNumber)
            .param("javaClassId", entity.javaClassId)
            .param("gcRoot", entity.gcRoot)
            .param("size", entity.size)
            .param("retainedSize", entity.retainedSize)
            .param("reachableSize", entity.reachableSize)
            .update()
    }

    override fun persistAll(entities: List<InstanceEntity>) {
        entities.forEach { persist(it) }
    }

    override fun findById(instanceId: Long): InstanceEntity? = jdbc
        .sql(
            """
            select instanceId,instanceNumber,javaClassId,gcRoot,size,retainedSize,reachableSize
            from Instance 
            where instanceId = :instanceId
        """
        )
        .param("instanceId", instanceId)
        .query(InstanceEntity::class.java)
        .optional().orElse(null)

    override fun streamAllByJavaClassId(javaClassId: Long): Stream<InstanceEntity> = jdbc
        .sql(
            """
            select instanceId,instanceNumber,javaClassId,gcRoot,size,retainedSize,reachableSize
            from Instance
            where javaClassId = :javaClassId
        """
        )
        .param("javaClassId", javaClassId)
        .query(InstanceEntity::class.java)
        .stream()
}