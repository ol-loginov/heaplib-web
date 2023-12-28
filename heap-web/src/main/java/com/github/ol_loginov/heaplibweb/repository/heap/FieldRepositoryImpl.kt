package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.support.GeneratedKeyHolder
import java.util.stream.Stream

internal class FieldRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldRepository {
    override fun persist(entity: FieldEntity) {
        val keyHolder = GeneratedKeyHolder()
        jdbc
            .sql("insert into Field(declaringClassId, name, staticFlag, typeId) values(:declaringClassId, :name, :staticFlag, :typeId)")
            .param("declaringClassId", entity.declaringClassId)
            .param("name", entity.name)
            .param("staticFlag", entity.staticFlag)
            .param("typeId", entity.typeId)
            .update(keyHolder)
        entity.id = keyHolder.key?.toInt() ?: throw IllegalStateException("no key returned while saving Field entity")
    }

    override fun persistAll(batch: List<FieldEntity>) {
        batch.forEach { persist(it) }
    }

    override fun findById(id: Int): FieldEntity? = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeId from Field where id = :id")
        .param("id", id)
        .query(FieldEntity::class.java)
        .optional().orElse(null)

    override fun streamAll(): Stream<FieldEntity> = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeId from Field order by id")
        .query(FieldEntity::class.java)
        .stream()

    override fun streamAllByDeclaringClassId(declaringClassId: Long): Stream<FieldEntity> = jdbc
        .sql("select id,declaringClassId,name,staticFlag,typeId from Field where declaringClassId =:declaringClassId order by id")
        .param("declaringClassId", declaringClassId)
        .query(FieldEntity::class.java)
        .stream()

}