package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import java.util.stream.Stream

interface FieldValueRepository {
    fun findAllReferenceValuesByInstance(instanceId: Long): List<FieldValueEntity>

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId
     */
    fun streamInstanceFieldValues(instanceId: Long): Stream<FieldValueEntity>

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId and F.name = :fieldName
     */
    fun findOneByInstanceAndFieldName(instanceId: Long, fieldName: String): FieldValueEntity?

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId
     */
    fun streamStaticFieldValues(declaringClassId: Long): Stream<FieldValueEntity>

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId and F.name = :fieldName
     */
    fun findStaticByClassAndFieldName(declaringClassId: Long, fieldName: String): FieldValueEntity?
}

internal class FieldValueRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : FieldValueRepository {

    override fun findAllReferenceValuesByInstance(instanceId: Long): List<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.valueInstanceId
             from FieldValue FV 
                inner join Field F on F.id = FV.fieldId 
            where F.typeTag in (:typeTags) and FV.instanceId = :instanceId
        """
        )
        .param("instanceId", instanceId)
        .param("typeTags", arrayOf(HprofValueType.Array.tag, HprofValueType.Object))
        .query(FieldValueEntity::class.java)
        .list()

    override fun streamInstanceFieldValues(instanceId: Long): Stream<FieldValueEntity> = jdbc
        .sql(
            """
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.valueInstanceId
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
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.valueInstanceId
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
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.valueInstanceId
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
            select FV.instanceId, FV.fieldId, FV.staticFlag, FV.valueInstanceId
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
