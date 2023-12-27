package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface FieldRepository {
    fun streamAllByDeclaringClassId(declaringClassId: Long): Stream<FieldEntity>

    fun streamAll(): Stream<FieldEntity>
}