package com.github.ol_loginov.heaplibweb.repository.heap

interface HeapRepository {
    fun persist(entity: HeapEntity): HeapEntity
    fun merge(entity: HeapEntity)

    fun findById(id: Int): HeapEntity?
    fun findByFileLatest(path: String): HeapEntity?
    fun findAllOrderByIdDesc(): List<HeapEntity>
    fun getScope(heapEntity: HeapEntity): HeapScope
}