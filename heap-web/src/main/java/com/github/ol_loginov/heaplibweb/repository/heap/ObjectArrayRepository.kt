package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface ObjectArrayRepository {
    fun streamNonNullItems(instanceId: Long): Stream<ObjectArrayEntity>
}

internal class ObjectArrayRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : ObjectArrayRepository {
    override fun streamNonNullItems(instanceId: Long): Stream<ObjectArrayEntity> = jdbc
        .sql("select instanceId, itemIndex, itemInstanceId from ObjectArrayEntity where instanceId = :instanceId and itemInstanceId > 0")
        .param("instanceId", instanceId)
        .query(ObjectArrayEntity::class.java)
        .stream()
}
