package com.github.ol_loginov.heaplibweb.repository

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.springframework.dao.TransientDataAccessResourceException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class HeapFileRepositoryImpl(
    private val jdbc: JdbcClient,
    private val jdbcOperations: NamedParameterJdbcOperations
) : HeapFileRepository {

    override fun persist(entity: HeapFile): HeapFile {
        val keyHolder = GeneratedKeyHolder()
        jdbc
            .sql("insert into HeapFile(path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix)  values(:path, :status,  :loadStart, :loadFinish,  :loadProgress,  :loadMessage, :loadError, :tablePrefix)")
            .param("path", entity.path)
            .param("status", entity.status.name)
            .param("loadStart", entity.loadStart)
            .param("loadFinish", entity.loadFinish)
            .param("loadProgress", entity.loadProgress)
            .param("loadMessage", entity.loadMessage)
            .param("loadError", entity.loadError)
            .param("tablePrefix", entity.tablePrefix)
            .update(keyHolder)
        entity.id = keyHolder.key?.toInt() ?: throw IllegalStateException("no id generated for HeapFile")
        return entity
    }

    override fun merge(entity: HeapFile) {
        val updated = jdbc
            .sql("update HeapFile set status = :status, loadStart = :loadStart, loadFinish = :loadFinish, loadProgress = :loadProgress, loadMessage = :loadMessage, loadError = :loadError, tablePrefix = :tablePrefix where id = :id")
            .param("id", entity.id)
            .param("status", entity.status.name)
            .param("loadStart", entity.loadStart)
            .param("loadFinish", entity.loadFinish)
            .param("loadProgress", entity.loadProgress)
            .param("loadMessage", entity.loadMessage)
            .param("loadError", entity.loadError)
            .param("tablePrefix", entity.tablePrefix)
            .update()
        if (updated != 1) throw TransientDataAccessResourceException("no HeapFile#${entity.id} to update")
    }

    override fun findById(entityId: Int): HeapFile? = jdbc
        .sql("select id, path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix from HeapFile where id = :id")
        .param("id", entityId)
        .query(HeapFile::class.java)
        .optional().orElse(null)

    override fun findAllByOrderByLoadStartDesc(): List<HeapFile> = jdbc
        .sql("select id, path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix from HeapFile order by loadStart desc")
        .query(HeapFile::class.java)
        .list()

    override fun findAllByStatusIn(statuses: List<HeapFileStatus>): List<HeapFile> = jdbc
        .sql("select id, path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix from HeapFile where (:statusesLen = 0 or status in (:statuses)) order by loadStart desc")
        .param("statusesLen", statuses.size)
        .param("statuses", statuses)
        .query(HeapFile::class.java)
        .list()

    override fun findAllByStatusNotIn(statuses: List<HeapFileStatus>): List<HeapFile> = jdbc
        .sql("select id, path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix from HeapFile where (:statusesLen = 0 or status not in (:statuses)) order by loadStart desc")
        .param("statusesLen", statuses.size)
        .param("statuses", statuses)
        .query(HeapFile::class.java)
        .list()

    override fun findFirstByPathOrderByIdDesc(path: String): HeapFile? = jdbc
        .sql("select id, path, status, loadStart, loadFinish, loadProgress, loadMessage, loadError, tablePrefix from HeapFile where path = :path order by id desc limit 1")
        .param("path", path)
        .query(HeapFile::class.java)
        .optional().orElse(null)

    override fun getScope(heapFile: HeapFile) = HeapScope(heapFile.tablePrefix, jdbc, jdbcOperations)
}