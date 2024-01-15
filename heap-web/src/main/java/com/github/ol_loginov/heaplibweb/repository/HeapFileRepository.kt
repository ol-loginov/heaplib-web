package com.github.ol_loginov.heaplibweb.repository

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories

interface HeapFileRepository {
    fun persist(entity: HeapFile): HeapFile
    fun merge(entity: HeapFile)

    fun findById(entityId: Int): HeapFile?

    fun findAllByOrderByLoadStartDesc(): List<HeapFile>

    fun findAllByStatusIn(statuses: List<HeapFileStatus>): List<HeapFile>

    fun findAllByStatusNotIn(statuses: List<HeapFileStatus>): List<HeapFile>

    fun findFirstByPathOrderByIdDesc(path: String): HeapFile?

    fun getHeapRepositories(heapFile: HeapFile): HeapRepositories
}