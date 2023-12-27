package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

internal class JavaClassRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : JavaClassRepository {
    override fun findByName(name: String): JavaClassEntity? = jdbc
        .sql(   """
            select javaClassId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId 
            from JavaClass 
            where name = :name
        """)
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
}