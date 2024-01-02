package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.util.stream.Stream

internal class FieldRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldRepository {
    private fun persistQueryParameters(entity: FieldEntity) = mapOf(
        "declaringClassId" to entity.declaringClassId,
        "name" to entity.name,
        "staticFlag" to entity.staticFlag,
        "typeTag" to entity.typeTag
    )

    override fun persist(entity: FieldEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(batch: List<FieldEntity>) {
        val keyHolder = GeneratedKeyHolder()
        val batchParameters = batch.map {
            MapSqlParameterSource(persistQueryParameters(it))
        }

        jdbc.batchUpdate(
            "insert into Field(declaringClassId, name, staticFlag, typeTag) values(:declaringClassId, :name, :staticFlag, :typeTag)",
            batchParameters,
            keyHolder
        )

        batch.indices.forEach {
            val key = keyHolder.keyList[it].values.first()
            batch[it].id = (key as Number).toInt()
        }
    }

    override fun findById(id: Int): FieldEntity? = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeTag from Field where id = :id")
        .param("id", id)
        .query(FieldEntity::class.java)
        .optional().orElse(null)

    override fun streamAll(): Stream<FieldEntity> = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeTag from Field order by id")
        .query(FieldEntity::class.java)
        .stream()

    override fun streamAllByDeclaringClassIdOrderById(declaringClassId: Long): Stream<FieldEntity> = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeTag from Field where declaringClassId =:declaringClassId order by id")
        .param("declaringClassId", declaringClassId)
        .query(FieldEntity::class.java)
        .stream()
}