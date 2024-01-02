package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface FieldRepository {
    fun persist(entity: FieldEntity)
    fun persistAll(batch: List<FieldEntity>)

    fun streamAllByDeclaringClassIdOrderById(declaringClassId: Long): Stream<FieldEntity>
    fun streamAll(): Stream<FieldEntity>

    fun findById(id: Int): FieldEntity?
}