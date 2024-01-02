package com.github.ol_loginov.heaplibweb.repository.heap

interface ObjectArrayRepository {
    fun persist(entity: ObjectArrayEntity)
    fun persistAll(entities: List<ObjectArrayEntity>)
}