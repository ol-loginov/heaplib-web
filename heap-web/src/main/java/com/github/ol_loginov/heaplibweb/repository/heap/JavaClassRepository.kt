package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface JavaClassRepository {
    fun findByName(name: String): JavaClassEntity?

    /**
     * select * from JavaClass where heapId = :heapId and regexp_like(name, :nameRegex)
     */
    fun findAllByNameRegex(nameRegex: String): Stream<JavaClassEntity>

    fun streamAll(): Stream<JavaClassEntity>

    fun streamAllBySuperClassId(superClassId: Long): Stream<JavaClassEntity>
}