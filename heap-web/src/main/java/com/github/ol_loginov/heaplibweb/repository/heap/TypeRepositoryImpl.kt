package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.support.GeneratedKeyHolder

internal class TypeRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : TypeRepository {
    override fun findById(typeId: Int): TypeEntity? = jdbc
        .sql("select id, name from Type where id = :id")
        .param("id", typeId)
        .query(TypeEntity::class.java)
        .optional().orElse(null)

    override fun persist(entity: TypeEntity): TypeEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbc
            .sql("insert into Type (name) values(:name)")
            .param("name", entity.name)
            .update(keyHolder)
        entity.id = keyHolder.key?.toInt() ?: throw IllegalStateException("no id generated for TypeEntity")
        return entity
    }
}