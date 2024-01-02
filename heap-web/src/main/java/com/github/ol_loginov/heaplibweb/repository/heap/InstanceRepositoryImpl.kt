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
        "rootTag" to entity.rootTag,
        "size" to entity.size,
        "arrayTypeTag" to entity.arrayTypeTag,
        "arrayLength" to entity.arrayLength,
        "retainedSize" to entity.retainedSize,
        "reachableSize" to entity.reachableSize
    )

    override fun persist(entity: InstanceEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<InstanceEntity>) {
        MultiValuesInsert(jdbc, "Instance").execute(entities.map { persistQueryParameters(it) })
    }

    override fun updateRoots(list: List<Pair<ULong, Short>>) {
        val batchParameters = list.map {
            MapSqlParameterSource(
                mapOf(
                    "instanceId" to it.first.toLong(),
                    "rootTag" to it.second
                )
            )
        }
        jdbc.batchUpdate("update Instance set rootTag = :rootTag where instanceId = :instanceId", batchParameters)
    }

    override fun findById(instanceId: Long): InstanceEntity? = jdbc
        .sql(
            """
            select instanceId,instanceNumber,javaClassId,rootTag,size,arrayTypeTag, arrayLength, retainedSize,reachableSize
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
            select instanceId,instanceNumber,javaClassId,rootTag,size,arrayTypeTag,arrayLength,retainedSize,reachableSize
            from Instance
            where javaClassId = :javaClassId
        """
        )
        .param("javaClassId", javaClassId)
        .query(InstanceEntity::class.java)
        .stream()
}