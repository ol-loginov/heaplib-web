package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class InstanceRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : InstanceRepository {
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