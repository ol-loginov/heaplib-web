package com.github.ol_loginov.heaplibweb.repository.heap

interface TypeRepository {
    fun persist(entity: TypeEntity): TypeEntity
    fun findById(typeId: Int): TypeEntity?
}