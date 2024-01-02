package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface ClassRepository {
    fun persist(entity: ClassEntity)
    fun persistAll(entities: List<ClassEntity>)

    fun updateCounts(list: List<Pair<ULong, Int>>)

    fun findById(id: Long): ClassEntity?
    fun findByIdSure(id: Long): ClassEntity
    fun findByName(name: String): ClassEntity?

    /**
     * select * from JavaClass where heapId = :heapId and regexp_like(name, :nameRegex)
     */
    fun findAllByNameRegex(nameRegex: String): Stream<ClassEntity>
    fun streamAll(): Stream<ClassEntity>
    fun streamAllBySuperClassId(superClassId: Long): Stream<ClassEntity>
}