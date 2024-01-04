package com.github.ol_loginov.heaplibweb.repository.heap

interface PrimitiveArrayRepository {
    fun persist(entity: PrimitiveArrayEntity)
    fun persistAll(entities: List<PrimitiveArrayEntity>)
}

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
        MultiValuesInsert(jdbc, "PrimitiveArray").execute(entities.map { persistQueryParameters(it) })
    }
}