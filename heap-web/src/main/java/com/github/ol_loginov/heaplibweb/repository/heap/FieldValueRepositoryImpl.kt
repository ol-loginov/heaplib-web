package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class FieldValueRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldValueRepository {
    private fun persistQueryParameters(entity: FieldValueEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "fieldId" to entity.fieldId,
        "value" to entity.value,
        "valueInstanceId" to entity.valueInstanceId
    )

    override fun persistAll(entities: List<FieldValueEntity>) {
        MultiValuesInsert(jdbc, "FieldValue").execute(entities.map { persistQueryParameters(it) })
    }

    override fun persist(entity: FieldValueEntity) {
        persistAll(listOf(entity))
    }

    override fun streamInstanceFieldValues(instanceId: Long): Stream<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.value, FV.valueInstanceId
             from FieldValue FV 
                inner join Field F on F.id = FV.fieldId 
            where F.staticFlag = 0 and FV.instanceId = :instanceId
        """
        )
        .param("instanceId", instanceId)
        .query(FieldValueEntity::class.java)
        .stream()

    override fun findOneByInstanceAndFieldName(instanceId: Long, fieldName: String): FieldValueEntity? = jdbc
        .sql(
            """
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.value, FV.valueInstanceId
            from FieldValue FV 
                inner join Field F on F.id = FV.fieldId 
            where F.staticFlag = 0 and FV.instanceId = :instanceId and F.name = :fieldName
        """
        )
        .param("instanceId", instanceId)
        .param("fieldName", fieldName)
        .query(FieldValueEntity::class.java)
        .optional().orElse(null)

    override fun streamStaticFieldValues(declaringClassId: Long): Stream<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.value, FV.valueInstanceId
            from FieldValue FV 
                inner join Field F on F.id = FV.fieldId 
            where F.staticFlag = 1 and FV.instanceId = :declaringClassId
        """
        )
        .param("declaringClassId", declaringClassId)
        .query(FieldValueEntity::class.java)
        .stream()

    override fun findStaticByClassAndFieldName(declaringClassId: Long, fieldName: String): FieldValueEntity? = jdbc
        .sql(
            """
            select FV.instanceId,FV.fieldId,FV.staticFlag,FV.value,FV.valueInstanceId
            from FieldValue FV
                inner join Field F on F.id = FV.fieldId 
            where F.staticFlag = 1 and FV.instanceId = :declaringClassId and F.name = :fieldName
        """
        )
        .param("declaringClassId", declaringClassId)
        .param("fieldName", fieldName)
        .query(FieldValueEntity::class.java)
        .optional().orElse(null)

}