package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource

internal class PrimitiveArrayRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : PrimitiveArrayRepository {
    private fun persistQueryParameters(entity: PrimitiveArrayEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "itemIndex" to entity.itemIndex,
        "itemValue" to entity.itemValue
    )

    override fun persist(entity: PrimitiveArrayEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<PrimitiveArrayEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate("insert into PrimitiveArray(instanceId, itemIndex, itemValue) values(:instanceId, :itemIndex, :itemValue)", batchParameters)
    }
}