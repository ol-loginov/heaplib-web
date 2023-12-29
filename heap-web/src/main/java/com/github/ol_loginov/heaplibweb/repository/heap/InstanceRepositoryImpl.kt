package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class InstanceRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : InstanceRepository {
    private fun persistQueryParameters(entity: InstanceEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "instanceNumber" to entity.instanceNumber,
        "javaClassId" to entity.javaClassId,
        "gcRoot" to entity.gcRoot,
        "size" to entity.size,
        "retainedSize" to entity.retainedSize,
        "reachableSize" to entity.reachableSize
    )

    override fun persist(entity: InstanceEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<InstanceEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate(
            """
                insert into Instance(instanceId, instanceNumber, javaClassId, gcRoot, size, retainedSize, reachableSize)
                values(:instanceId, :instanceNumber, :javaClassId, :gcRoot, :size, :retainedSize, :reachableSize)
            """,
            batchParameters
        )
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