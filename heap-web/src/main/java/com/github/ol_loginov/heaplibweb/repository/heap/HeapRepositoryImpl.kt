package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.HeapFile
import jakarta.inject.Inject
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
private class HeapRepositoryImpl @Inject constructor(
    private val jdbc: JdbcClient
) : HeapRepository {
    override fun findByFile(heapFile: HeapFile): HeapEntity? = jdbc
        .sql("select Id,fileId,tm from Heap where fileId = :fileId")
        .param("fileId", heapFile.id)
        .query(HeapEntity::class.java)
        .optional().orElse(null)

    override fun findAllOrderByIdDesc(): List<HeapEntity> = jdbc
        .sql("select Id,fileId,tm from Heap order by id desc")
        .query(HeapEntity::class.java)
        .list()

    override fun createScope(heapEntity: HeapEntity) = HeapScope(heapEntity, jdbc)
}