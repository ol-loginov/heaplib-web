package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface NameRepository {
    fun persistAll(entities: List<NameEntity>)

    fun streamAll(): Stream<NameEntity>
}