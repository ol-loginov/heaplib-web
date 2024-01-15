package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface FieldRepository {
    fun persist(entity: FieldEntity)
    fun persistAll(entities: List<FieldEntity>)

    fun findAllByDeclaringClassIdOrderById(declaringClassId: Long): List<FieldEntity>
    fun streamAllOrderById(): Stream<FieldEntity>

    fun findById(id: Int): FieldEntity?
    fun findByDeclaringClassIdAndName(declaringClassId: Long, name: String): FieldEntity?
}