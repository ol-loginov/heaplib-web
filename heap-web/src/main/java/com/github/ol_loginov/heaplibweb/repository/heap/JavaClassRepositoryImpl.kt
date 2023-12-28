package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.dao.TransientDataAccessResourceException
import java.util.stream.Stream

internal class JavaClassRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : JavaClassRepository {
    override fun findById(id: Long): JavaClassEntity? = jdbc
        .sql("select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from JavaClass where id = :id")
        .param("id", id)
        .query(JavaClassEntity::class.java)
        .optional().orElse(null)

    override fun findByIdSure(id: Long): JavaClassEntity = findById(id) ?: throw TransientDataAccessResourceException("no JavaClass#${id}")

    override fun findByName(name: String): JavaClassEntity? = jdbc
        .sql(
            """
            select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
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
            select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where regexp_like(name, :nameRegex)
        """
        )
        .param("nameRegex", nameRegex)
        .query(JavaClassEntity::class.java)
        .stream()

    override fun streamAll(): Stream<JavaClassEntity> = jdbc
        .sql("select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId from JavaClass")
        .query(JavaClassEntity::class.java)
        .stream()

    override fun streamAllBySuperClassId(superClassId: Long): Stream<JavaClassEntity> = jdbc
        .sql(
            """
            select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where superClassId = :superClassId
        """
        )
        .param("superClassId", superClassId)
        .query(JavaClassEntity::class.java)
        .stream()

    override fun persist(entity: JavaClassEntity) {
        jdbc
            .sql(
                """
                insert into JavaClass(javaClassId, name, allInstancesSize, array, instanceSize, instancesCount, retainedSizeByClass, superClassId) 
                values(:javaClassId, :name, :allInstancesSize, :array, :instanceSize, :instancesCount, :retainedSizeByClass, :superClassId)
            """
            )
            .param("javaClassId", entity.javaClassId)
            .param("name", entity.name)
            .param("allInstancesSize", entity.allInstancesSize)
            .param("array", entity.array)
            .param("instanceSize", entity.instanceSize)
            .param("instancesCount", entity.instancesCount)
            .param("retainedSizeByClass", entity.retainedSizeByClass)
            .param("superClassId", entity.superClassId)
            .update()
    }
}