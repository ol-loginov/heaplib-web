package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

internal class FieldRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldRepository {
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