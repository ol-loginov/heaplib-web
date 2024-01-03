package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.util.stream.Stream

internal class FieldRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldRepository {
    private fun persistQueryParameters(entity: FieldEntity) = mapOf(
        "declaringClassId" to entity.declaringClassId,
        "nameId" to entity.nameId,
        "staticFlag" to entity.staticFlag,
        "typeTag" to entity.typeTag
    )

    override fun persist(entity: FieldEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<FieldEntity>) {
        MultiValuesInsert(jdbc, "Field").execute(entities.map { persistQueryParameters(it) })
    }

    override fun findById(id: Int): FieldEntity? = jdbc
        .sql("select id,declaringClassId,nameId,staticFlag,typeTag from Field where id = :id")
        .param("id", id)
        .query(FieldEntity::class.java)
        .optional().orElse(null)

    override fun streamAllOrderById(): Stream<FieldEntity> = jdbc
        .sql("select id,declaringClassId,nameId,staticFlag,typeTag from Field order by id")
        .query(FieldEntity::class.java)
        .stream()

    override fun findAllByDeclaringClassIdOrderById(declaringClassId: Long): List<FieldEntity> = jdbc
        .sql("select id,declaringClassId,nameId,staticFlag,typeTag from Field where declaringClassId =:declaringClassId order by id")
        .param("declaringClassId", declaringClassId)
        .query(FieldEntity::class.java)
        .list()
}