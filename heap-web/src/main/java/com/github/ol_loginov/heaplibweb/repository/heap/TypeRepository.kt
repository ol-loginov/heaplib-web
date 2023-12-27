package com.github.ol_loginov.heaplibweb.repository.heap

interface TypeRepository {
    fun findById(typeId: Int): TypeEntity
}