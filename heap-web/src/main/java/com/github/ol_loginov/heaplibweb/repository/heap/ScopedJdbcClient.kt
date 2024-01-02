package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.KeyHolder

internal class ScopedJdbcClient(
    private val tablePrefix: String,
    private val delegate: JdbcClient,
    private val jdbcOperations: NamedParameterJdbcOperations
) : JdbcClient {
    companion object {
        val TABLES = arrayOf("FieldValue", "Field", "Instance", "Class", "Type", "PrimitiveArray", "ObjectArray")
    }

    private val regex = Regex("\\b(" + TABLES.joinToString("|") + ")\\b")

    override fun sql(sql: String): JdbcClient.StatementSpec {
        return delegate.sql(scopedSql(sql))
    }

    fun scopedSql(sql: String) = regex.replace(sql, "${tablePrefix}$1")

    fun update(sql: String, paramSource: SqlParameterSource): Int {
        return jdbcOperations.update(scopedSql(sql), paramSource)
    }

    fun batchUpdate(sql: String, batchValues: List<SqlParameterSource>): IntArray {
        return jdbcOperations.batchUpdate(scopedSql(sql), batchValues.toTypedArray())
    }

    fun batchUpdate(sql: String, batchValues: List<SqlParameterSource>, generatedKeyHolder: KeyHolder): IntArray {
        return jdbcOperations.batchUpdate(scopedSql(sql), batchValues.toTypedArray(), generatedKeyHolder)
    }
}