package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface FieldValueRepository {
    fun persist(entity: FieldValueEntity)
    fun persistAll(entities: List<FieldValueEntity>)

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId
     */
    fun streamInstanceFieldValues(definingInstanceId: Long): Stream<FieldValueEntity>

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId and F.name = :fieldName
     */
    fun findOneByInstanceAndFieldName(definingInstanceId: Long, fieldName: String): FieldValueEntity?

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId
     */
    fun streamStaticFieldValues(declaringClassId: Long): Stream<FieldValueEntity>

    /**
     * select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId and F.name = :fieldName
     */
    fun findStaticByClassAndFieldName(declaringClassId: Long, fieldName: String): FieldValueEntity?
}