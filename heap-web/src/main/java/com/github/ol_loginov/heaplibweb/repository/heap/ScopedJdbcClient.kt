package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.JdbcClient

internal class ScopedJdbcClient(
    private val tablePrefix: String,
    private val delegate: JdbcClient,
    private val jdbcOperations: NamedParameterJdbcOperations
) : JdbcClient {
    private companion object {
        val TABLES = setOf(
            "JavaClass",
            "Field",
            "Type",
            "Instance",
            "FieldValue"
        )
    }

    private val regex = Regex("\\b(" + TABLES.joinToString("|") + ")\\b")

    override fun sql(sql: String): JdbcClient.StatementSpec {
        return delegate.sql(scopedSql(sql))
    }

    fun scopedSql(sql: String) = regex.replace(sql, "${tablePrefix}$1")

    fun batchUpdate(sql: String, batchValues: List<SqlParameterSource>): IntArray {
        return jdbcOperations.batchUpdate(scopedSql(sql), batchValues.toTypedArray())
    }
}