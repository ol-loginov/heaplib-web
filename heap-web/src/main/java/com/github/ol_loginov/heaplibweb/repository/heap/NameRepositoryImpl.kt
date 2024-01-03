package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

internal class NameRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : NameRepository {
    private fun persistQueryParameters(entity: NameEntity) = mapOf(
        "name" to entity.name,
    )

    override fun persistAll(entities: List<NameEntity>) {
        MultiValuesInsert(jdbc, "Name").execute(entities.map { persistQueryParameters(it) })
    }

    override fun streamAll(): Stream<NameEntity> = jdbc
        .sql("select id, name from Name")
        .query(NameEntity::class.java)
        .stream()
}