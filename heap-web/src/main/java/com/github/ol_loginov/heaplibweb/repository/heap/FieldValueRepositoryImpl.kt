package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class FieldValueRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldValueRepository {
    private fun persistQueryParameters(entity: FieldValueEntity) = mapOf(
        "javaClassId" to entity.javaClassId,
        "definingInstanceId" to entity.definingInstanceId,
        "fieldId" to entity.fieldId,
        "staticFlag" to entity.staticFlag,
        "value" to entity.value,
        "valueInstanceId" to entity.valueInstanceId
    )

    override fun persistAll(entities: List<FieldValueEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate(
            """
                insert into FieldValue(javaClassId, definingInstanceId, fieldId, staticFlag, value, valueInstanceId) 
                values(:javaClassId, :definingInstanceId, :fieldId, :staticFlag, :value, :valueInstanceId)
            """,
            batchParameters
        )
    }

    override fun persist(entity: FieldValueEntity) {
        persistAll(listOf(entity))
    }

    override fun streamInstanceFieldValues(definingInstanceId: Long): Stream<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.javaClassId,FV.definingInstanceId,FV.fieldId,FV.staticFlag,FV.value,FV.valueInstanceId
             from FieldValue FV 
                inner  join Field F on F.id = FV.fieldId 
            where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId
        """
        )
        .param("definingInstanceId", definingInstanceId)
        .query(FieldValueEntity::class.java)
        .stream()

    override fun findOneByInstanceAndFieldName(definingInstanceId: Long, fieldName: String): FieldValueEntity? = jdbc
        .sql(
            """
            select FV.javaClassId,FV.definingInstanceId,FV.fieldId,FV.staticFlag,FV.value,FV.valueInstanceId
            from FieldValue FV 
                inner  join Field F on F.id = FV.fieldId 
            where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId and F.name = :fieldName
        """
        )
        .param("definingInstanceId", definingInstanceId)
        .param("fieldName", fieldName)
        .query(FieldValueEntity::class.java)
        .optional().orElse(null)

    override fun streamStaticFieldValues(declaringClassId: Long): Stream<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.javaClassId,FV.definingInstanceId,FV.fieldId,FV.staticFlag,FV.value,FV.valueInstanceId
            from FieldValue FV 
                inner  join Field F on F.id = FV.fieldId
            where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId
        """
        )
        .param("declaringClassId", declaringClassId)
        .query(FieldValueEntity::class.java)
        .stream()

    override fun findStaticByClassAndFieldName(declaringClassId: Long, fieldName: String): FieldValueEntity? = jdbc
        .sql(
            """
            select FV.javaClassId,FV.definingInstanceId,FV.fieldId,FV.staticFlag,FV.value,FV.valueInstanceId
            from FieldValue FV 
                inner  join Field F on F.id = FV.fieldId 
            where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId and F.name = :fieldName
        """
        )
        .param("declaringClassId", declaringClassId)
        .param("fieldName", fieldName)
        .query(FieldValueEntity::class.java)
        .optional().orElse(null)

}