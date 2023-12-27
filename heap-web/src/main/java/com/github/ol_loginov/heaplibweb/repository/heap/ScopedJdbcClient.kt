package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.simple.JdbcClient

internal class ScopedJdbcClient(
    private val tablePrefix: String,
    private val delegate: JdbcClient
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
        return delegate.sql(regex.replace(sql, "${tablePrefix}$1"))
    }
}