package com.github.ol_loginov.heaplibweb.repository

import org.springframework.dao.TransientDataAccessResourceException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

@Repository
class HeapFileRepositoryImpl(
    private val jdbc: JdbcClient
) : HeapFileRepository {

    override fun persist(entity: HeapFile): HeapFile {
        val keyHolder = GeneratedKeyHolder()
        jdbc
            .sql("insert HeapFile(status, loadStart, loadFinish, loadProgress, loadMessage, loadError)  values(:status,  :loadStart, :loadFinish,  :loadProgress,  :loadMessage, :loadError")
            .param("status", entity.status)
            .param("loadStart", entity.loadStart)
            .param("loadFinish", entity.loadFinish)
            .param("loadProgress", entity.loadProgress)
            .param("loadMessage", entity.loadMessage)
            .param("loadError", entity.loadError)
            .update(keyHolder)
        entity.id = keyHolder.key?.toInt() ?: throw IllegalStateException("no id generated for HeapFile")
        return entity
    }

    override fun merge(entity: HeapFile) {
        val updated = jdbc
            .sql("update HeapFile set status = :status, loadStart = :loadStart, loadFinish = :loadFinish, loadProgress = :loadProgress, loadMessage = :loadMessage, loadError = :loadError where id = :id")
            .param("id", entity.id)
            .param("status", entity.status)
            .param("loadStart", entity.loadStart)
            .param("loadFinish", entity.loadFinish)
            .param("loadProgress", entity.loadProgress)
            .param("loadMessage", entity.loadMessage)
            .param("loadError", entity.loadError)
            .update()
        if (updated != 1) throw TransientDataAccessResourceException("no HeapFile#${entity.id} to update")
    }

    override fun findById(entityId: Int): HeapFile? = jdbc
        .sql("select id, relativePath, status, loadStart, loadFinish, loadProgress, loadMessage, loadError from HeapFile where id = :id")
        .param("id", entityId)
        .query(HeapFile::class.java)
        .optional().orElse(null)

    override fun findAllByOrderByLoadStartDesc(): List<HeapFile> = jdbc
        .sql("select id, relativePath, status, loadStart, loadFinish, loadProgress, loadMessage, loadError from HeapFile order by loadStart desc")
        .query(HeapFile::class.java)
        .list()

    override fun findAllByStatusIn(statuses: List<HeapFileStatus>): List<HeapFile> = jdbc
        .sql("select id, relativePath, status, loadStart, loadFinish, loadProgress, loadMessage, loadError from HeapFile where (:statusesLen = 0 or status in (:statuses)) order by loadStart desc")
        .param("statusesLen", statuses.size)
        .param("statuses", statuses)
        .query(HeapFile::class.java)
        .list()

    override fun findAllByStatusNotIn(statuses: List<HeapFileStatus>): List<HeapFile> = jdbc
        .sql("select id, relativePath, status, loadStart, loadFinish, loadProgress, loadMessage, loadError from HeapFile where (:statusesLen = 0 or status not in (:statuses)) order by loadStart desc")
        .param("statusesLen", statuses.size)
        .param("statuses", statuses)
        .query(HeapFile::class.java)
        .list()

    override fun findFirstByRelativePathOrderByIdDesc(relativePath: String): HeapFile? = jdbc
        .sql("select id, relativePath, status, loadStart, loadFinish, loadProgress, loadMessage, loadError from HeapFile where relativePath = :relativePath order by id desc limit 1")
        .param("relativePath", relativePath)
        .query(HeapFile::class.java)
        .optional().orElse(null)
}