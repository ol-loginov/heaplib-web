package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

internal class FieldValueRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldValueRepository {
    override fun persistAll(entities: List<FieldValueEntity>) {
        entities.forEach { e -> persist(e) }
    }

    override fun persist(entity: FieldValueEntity) {
        jdbc
            .sql(
                """
                insert into FieldValue(javaClassId, definingInstanceId, fieldId, staticFlag, value, valueInstanceId) 
                values(:javaClassId, :definingInstanceId, :fieldId, :staticFlag, :value, :valueInstanceId)
            """
            )
            .param("javaClassId", entity.javaClassId)
            .param("definingInstanceId", entity.definingInstanceId)
            .param("fieldId", entity.fieldId)
            .param("staticFlag", entity.staticFlag)
            .param("value", entity.value)
            .param("valueInstanceId", entity.valueInstanceId)
            .update()
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