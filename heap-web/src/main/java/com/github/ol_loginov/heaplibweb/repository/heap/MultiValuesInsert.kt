package com.github.ol_loginov.heaplibweb.repository.heap

import kotlin.math.min

internal class MultiValuesInsert(
    private val jdbc: ScopedJdbcClient,
    private val tableName: String,
    private val multiValuesLength: Int = 10
) {
    private var sqlPrefix: String = ""

    fun execute(batch: List<Map<String, Any?>>): Int {
        var offset = 0
        while (offset < batch.size) {
            offset += execute(batch, offset)
        }
        return offset
    }

    private fun execute(batch: List<Map<String, Any?>>, start: Int): Int {
        val count = min(start + multiValuesLength, batch.size) - start
        if (count <= 0) return 0

        val fields = batch[0].keys
        if (sqlPrefix.isEmpty()) {
            sqlPrefix = "insert into $tableName (${fields.joinToString(",")}) values "
        }

        var sql = sqlPrefix
        val parameters = mutableListOf<Any?>()
        val placeholders = "(" + ",?".repeat(fields.size).substring(1) + ")"
        IntRange(start, start + count - 1).map { index ->
            if (index > start)
                sql += ","
            sql += placeholders
            parameters.addAll(batch[index].values)
        }

        jdbc
            .sql(sql)
            .params(parameters)
            .update()

        return count
    }
}