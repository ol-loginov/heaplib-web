package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface JavaClassRepository {
    fun persist(entity: JavaClassEntity)
    fun persistAll(entities: List<JavaClassEntity>)

    fun updateCounts(list: List<Pair<ULong, Int>>)

    fun findById(id: Long): JavaClassEntity?
    fun findByIdSure(id: Long): JavaClassEntity
    fun findByName(name: String): JavaClassEntity?

    /**
     * select * from JavaClass where heapId = :heapId and regexp_like(name, :nameRegex)
     */
    fun findAllByNameRegex(nameRegex: String): Stream<JavaClassEntity>
    fun streamAll(): Stream<JavaClassEntity>
    fun streamAllBySuperClassId(superClassId: Long): Stream<JavaClassEntity>
}