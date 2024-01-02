package com.github.ol_loginov.heaplibweb.repository.heap

interface PrimitiveArrayRepository {
    fun persist(entity: PrimitiveArrayEntity)
    fun persistAll(entities: List<PrimitiveArrayEntity>)
}