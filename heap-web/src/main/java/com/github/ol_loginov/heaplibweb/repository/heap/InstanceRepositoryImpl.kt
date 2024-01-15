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
            select instanceId,fo,instanceNumber,javaClassId,rootTag,size,arrayTypeTag, arrayLength, retainedSize,reachableSize
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
            select instanceId,fo,instanceNumber,javaClassId,rootTag,size,arrayTypeTag,arrayLength,retainedSize,reachableSize
            from Instance
            where javaClassId = :javaClassId
        """
        )
        .param("javaClassId", javaClassId)
        .query(InstanceEntity::class.java)
        .stream()

    override fun findAllAfterId(afterId: Long, limit: Int): List<InstanceEntity> = jdbc
        .sql(
            """
            select instanceId,fo,instanceNumber,javaClassId,rootTag,size,arrayTypeTag,arrayLength,retainedSize,reachableSize
            from Instance
            where instanceId > :afterId
        """
        )
        .param("afterId", afterId)
        .query(InstanceEntity::class.java)
        .list()

    override fun findAllRoots(): List<InstanceEntity> = jdbc
        .sql(
            """
            select instanceId,fo,instanceNumber,javaClassId,rootTag,size,arrayTypeTag,arrayLength,retainedSize,reachableSize
            from Instance
            where rootTag > 0
        """
        )
        .query(InstanceEntity::class.java)
        .list()

    override fun findAllRootInstances(): List<Long> = jdbc
        .sql("select instanceId from Instance where rootTag > 0")
        .query(Long::class.java)
        .list()

    override fun clearRetainedSize() = jdbc
        .sql("update Instance set retainedSize = null where 1 = 1")
        .update()

    override fun updateRetainedSizeByClass(javaClassId: Long, retainedSize: Long) = jdbc
        .sql("update Instance set retainedSize = :retainedSize where javaClassId = :javaClassId")
        .param("javaClassId", javaClassId)
        .param("retainedSize", retainedSize)
        .update()
}