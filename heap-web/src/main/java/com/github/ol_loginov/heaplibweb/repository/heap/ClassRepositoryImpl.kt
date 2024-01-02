package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.dao.TransientDataAccessResourceException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class ClassRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : ClassRepository {
    private fun persistQueryParameters(entity: ClassEntity) = mapOf(
        "id" to entity.id,
        "classLoaderObjectId" to entity.classLoaderObjectId,
        "name" to entity.name,
        "allInstancesSize" to entity.allInstancesSize,
        "array" to entity.array,
        "instanceSize" to entity.instanceSize,
        "instancesCount" to entity.instancesCount,
        "retainedSizeByClass" to entity.retainedSizeByClass,
        "superClassId" to entity.superClassId
    )

    override fun persist(entity: ClassEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<ClassEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate(
            """
                insert into Class(id, classLoaderObjectId, name, allInstancesSize, array, instanceSize, instancesCount, retainedSizeByClass, superClassId) 
                values(:id, :classLoaderObjectId, :name, :allInstancesSize, :array, :instanceSize, :instancesCount, :retainedSizeByClass, :superClassId)
            """,
            batchParameters
        )
    }

    override fun updateCounts(list: List<Pair<ULong, Int>>) {
        val batchParameters = list.map {
            MapSqlParameterSource(
                mapOf(
                    "id" to it.first.toLong(),
                    "instancesCount" to it.second
                )
            )
        }
        jdbc.batchUpdate("update Class set instancesCount = :instancesCount where id = :id", batchParameters)
    }

    override fun findById(id: Long): ClassEntity? = jdbc
        .sql("select id, classLoaderObjectId, name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from Class where id = :id")
        .param("id", id)
        .query(ClassEntity::class.java)
        .optional().orElse(null)

    override fun findByIdSure(id: Long): ClassEntity = findById(id) ?: throw TransientDataAccessResourceException("no Class#${id}")

    override fun findByName(name: String): ClassEntity? = jdbc
        .sql(
            """
            select id,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from Class 
            where name = :name
        """
        )
        .param("name", name)
        .query(ClassEntity::class.java)
        .optional().orElse(null)

    override fun findAllByNameRegex(nameRegex: String): Stream<ClassEntity> = jdbc
        .sql(
            """
            select id,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from Class 
            where regexp_like(name, :nameRegex)
        """
        )
        .param("nameRegex", nameRegex)
        .query(ClassEntity::class.java)
        .stream()

    override fun streamAll(): Stream<ClassEntity> = jdbc
        .sql("select id,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from Class")
        .query(ClassEntity::class.java)
        .stream()

    override fun streamAllBySuperClassId(superClassId: Long): Stream<ClassEntity> = jdbc
        .sql(
            """
            select id,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from Class 
            where superClassId = :superClassId
        """
        )
        .param("superClassId", superClassId)
        .query(ClassEntity::class.java)
        .stream()

}