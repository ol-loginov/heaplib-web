package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.dao.TransientDataAccessResourceException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import java.util.stream.Stream

internal class JavaClassRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : JavaClassRepository {
    private fun persistQueryParameters(entity: JavaClassEntity) = mapOf(
        "javaClassId" to entity.javaClassId,
        "classLoaderObjectId" to entity.classLoaderObjectId,
        "name" to entity.name,
        "allInstancesSize" to entity.allInstancesSize,
        "array" to entity.array,
        "instanceSize" to entity.instanceSize,
        "instancesCount" to entity.instancesCount,
        "retainedSizeByClass" to entity.retainedSizeByClass,
        "superClassId" to entity.superClassId
    )

    override fun persist(entity: JavaClassEntity) {
        persistAll(listOf(entity))
    }

    override fun persistAll(entities: List<JavaClassEntity>) {
        val batchParameters = entities.map { MapSqlParameterSource(persistQueryParameters(it)) }
        jdbc.batchUpdate(
            """
                insert into JavaClass(javaClassId, classLoaderObjectId, name, allInstancesSize, array, instanceSize, instancesCount, retainedSizeByClass, superClassId) 
                values(:javaClassId, :classLoaderObjectId, :name, :allInstancesSize, :array, :instanceSize, :instancesCount, :retainedSizeByClass, :superClassId)
            """,
            batchParameters
        )
    }

    override fun updateCounts(list: List<Pair<ULong, Int>>) {
        val batchParameters = list.map {
            MapSqlParameterSource(
                mapOf(
                    "javaClassId" to it.first.toLong(),
                    "instancesCount" to it.second
                )
            )
        }
        jdbc.batchUpdate("update JavaClass set instancesCount = :instancesCount where javaClassId = :javaClassId", batchParameters)
    }

    override fun findById(id: Long): JavaClassEntity? = jdbc
        .sql("select javaClassId, classLoaderObjectId, name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from JavaClass where id = :id")
        .param("id", id)
        .query(JavaClassEntity::class.java)
        .optional().orElse(null)

    override fun findByIdSure(id: Long): JavaClassEntity = findById(id) ?: throw TransientDataAccessResourceException("no JavaClass#${id}")

    override fun findByName(name: String): JavaClassEntity? = jdbc
        .sql(
            """
            select javaClassId,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where name = :name
        """
        )
        .param("name", name)
        .query(JavaClassEntity::class.java)
        .optional().orElse(null)

    override fun findAllByNameRegex(nameRegex: String): Stream<JavaClassEntity> = jdbc
        .sql(
            """
            select javaClassId,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where regexp_like(name, :nameRegex)
        """
        )
        .param("nameRegex", nameRegex)
        .query(JavaClassEntity::class.java)
        .stream()

    override fun streamAll(): Stream<JavaClassEntity> = jdbc
        .sql("select javaClassId,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from JavaClass")
        .query(JavaClassEntity::class.java)
        .stream()

    override fun streamAllBySuperClassId(superClassId: Long): Stream<JavaClassEntity> = jdbc
        .sql(
            """
            select javaClassId,classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where superClassId = :superClassId
        """
        )
        .param("superClassId", superClassId)
        .query(JavaClassEntity::class.java)
        .stream()

}