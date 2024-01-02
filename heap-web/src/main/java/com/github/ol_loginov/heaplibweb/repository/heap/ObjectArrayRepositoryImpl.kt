package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

internal class ObjectArrayRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : ObjectArrayRepository {
    private fun persistQueryParameters(entity: ObjectArrayEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "itemIndex" to entity.itemIndex,
        "itemInstanceId" to entity.itemInstanceId
    )

    override fun persist(entity: ObjectArrayEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<ObjectArrayEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate("insert into ObjectArray(instanceId, itemIndex, itemInstanceId) values(:instanceId, :itemIndex, :itemInstanceId)", batchParameters)
    }
}