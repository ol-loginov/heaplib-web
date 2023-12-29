package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.HeapFile
import jakarta.inject.Inject
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
private class HeapRepositoryImpl @Inject constructor(
    private val jdbc: JdbcClient,
    private val jdbcOperations: NamedParameterJdbcOperations
) : HeapRepository {
    override fun persist(entity: HeapEntity): HeapEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbc
            .sql("insert into Heap(fileId, tm, tablePrefix) values(:fileId, :tm, :tablePrefix)")
            .param("fileId", entity.fileId)
            .param("tm", entity.tm)
            .param("tablePrefix", entity.tablePrefix)
            .update(keyHolder)
        entity.id = keyHolder.key?.toInt() ?: throw IllegalStateException("no id generated for HeapEntity")
        return entity
    }

    override fun merge(entity: HeapEntity) {
        jdbc
            .sql(
                """
                update Heap
                set fileId = :fileId, tm = :tm, tablePrefix = :tablePrefix
                where id = :id
            """
            )
            .param("id", entity.id)
            .param("fileId", entity.fileId)
            .param("tm", entity.tm)
            .param("tablePrefix", entity.tablePrefix)
            .update()
    }

    override fun findById(id: Int): HeapEntity? = jdbc
        .sql("select Id,fileId,tm,tablePrefix from Heap where id = :id")
        .param("id", id)
        .query(HeapEntity::class.java)
        .optional().orElse(null)

    override fun findByFileLatest(path: String): HeapEntity? = jdbc
        .sql("""
            select H.Id, H.fileId, H.tm, H.tablePrefix 
            from Heap H
                inner join HeapFile HF on H.fileId = HF.id
            where HF.path = :path
            order by HF.id desc
            limit 1
        """)
        .param("path", path)
        .query(HeapEntity::class.java)
        .optional().orElse(null)

    override fun findAllOrderByIdDesc(): List<HeapEntity> = jdbc
        .sql("select Id,fileId,tm,tablePrefix from Heap order by id desc")
        .query(HeapEntity::class.java)
        .list()

    override fun getScope(heapEntity: HeapEntity) = HeapScope(heapEntity, jdbc, jdbcOperations)
}