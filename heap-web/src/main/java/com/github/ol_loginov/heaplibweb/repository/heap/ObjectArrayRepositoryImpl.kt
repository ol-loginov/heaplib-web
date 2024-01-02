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
        MultiValuesInsert(jdbc, "ObjectArray").execute(entities.map { persistQueryParameters(it) })
    }
}