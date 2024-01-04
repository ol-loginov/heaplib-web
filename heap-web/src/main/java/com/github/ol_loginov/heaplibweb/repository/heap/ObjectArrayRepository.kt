package com.github.ol_loginov.heaplibweb.repository.heap

interface ObjectArrayRepository {
}

internal class ObjectArrayRepositoryImpl(
    private val jdbc: ScopedJdbcClient
) : ObjectArrayRepository
