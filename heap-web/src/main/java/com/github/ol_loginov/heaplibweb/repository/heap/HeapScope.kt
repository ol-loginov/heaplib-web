package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.simple.JdbcClient

class HeapScope(
    val heap: HeapEntity,
    private val jdbc: JdbcClient,
) {
    companion object {
        const val NUMBER_NOT_READY = -1

        @JvmStatic
        fun shouldBeReady(entityAttribute: Int): Int {
            if (entityAttribute == NUMBER_NOT_READY) {
                throw UnsupportedOperationException("number not ready")
            }
            return entityAttribute
        }

        @JvmStatic
        fun shouldBeReady(entityAttribute: Long): Long {
            if (entityAttribute == NUMBER_NOT_READY.toLong()) {
                throw UnsupportedOperationException("number not ready")
            }
            return entityAttribute
        }
    }

    private val tablePrefix = "H" + heap.id + "_"

    val types: TypeRepository by lazy { TypeRepositoryImpl(ScopedJdbcClient(tablePrefix, jdbc)) }
    val javaClasses: JavaClassRepository by lazy { JavaClassRepositoryImpl(ScopedJdbcClient(tablePrefix, jdbc)) }
    val fields: FieldRepository by lazy { FieldRepositoryImpl(ScopedJdbcClient(tablePrefix, jdbc)) }
    val fieldValues: FieldValueRepository by lazy { FieldValueRepositoryImpl(ScopedJdbcClient(tablePrefix, jdbc)) }
    val instances: InstanceRepository by lazy { InstanceRepositoryImpl(ScopedJdbcClient(tablePrefix, jdbc)) }
}