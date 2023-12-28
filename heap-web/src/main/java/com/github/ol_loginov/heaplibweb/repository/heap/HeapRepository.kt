package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.HeapFile

interface HeapRepository {
    fun persist(entity: HeapEntity): HeapEntity
    fun findByFile(heapFile: HeapFile): HeapEntity?
    fun findAllOrderByIdDesc(): List<HeapEntity>
    fun createScope(heapEntity: HeapEntity): HeapScope
}