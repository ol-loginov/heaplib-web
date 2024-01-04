package com.github.ol_loginov.heaplibweb.repository.heap

import org.netbeans.lib.profiler.heap.HeapOperationUnsupportedException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.simple.JdbcClient
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.LongSupplier

class HeapScope(
    private val tablePrefix: String,
    private val jdbc: JdbcClient,
    private val jdbcOperations: NamedParameterJdbcOperations
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
        fun notReadyValueOnError(supplier: LongSupplier): Long {
            try {
                return supplier.getAsLong();
            } catch (e: HeapOperationUnsupportedException) {
                return NUMBER_NOT_READY.toLong()
            }
        }
    }

    private val scopedJdbcClient = ScopedJdbcClient(tablePrefix, jdbc, jdbcOperations)
    private val localFileFolder: Path = Paths.get(System.getProperty("java.io.tmpdir"))

    val names: NameRepository by lazy { NameRepositoryImpl(scopedJdbcClient) }
    val classes: ClassRepository by lazy { ClassRepositoryImpl(scopedJdbcClient) }
    val classLoader: ClassLoader by lazy { ClassLoaderByLocalFile(localFileFolder, scopedJdbcClient) }
    val fields: FieldRepository by lazy { FieldRepositoryImpl(scopedJdbcClient) }
    val fieldValues: FieldValueRepository by lazy { FieldValueRepositoryImpl(scopedJdbcClient) }
    val instances: InstanceRepository by lazy { InstanceRepositoryImpl(scopedJdbcClient) }
    val primitiveArrayItems: PrimitiveArrayRepository by lazy { PrimitiveArrayRepositoryImpl(scopedJdbcClient) }
    val objectArrayItems: ObjectArrayRepository by lazy { ObjectArrayRepositoryImpl(scopedJdbcClient) }

    init {

    }

    private fun executeTablesScript(scriptResource: String) {
        val script = HeapScope::class.java.getResourceAsStream(scriptResource).use {
            if (it == null)
                throw IllegalStateException("create scope tables script is not available");
            it.reader(StandardCharsets.UTF_8).readText()
        }

        val tableNameRegex = Regex("\\b(" + ScopedJdbcClient.TABLES.joinToString("|") + ")(_|\\b)")
        script.split(';')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { tableNameRegex.replace(it, "${tablePrefix}$1$2") }
            .forEach { sql -> jdbc.sql(sql).update() }
    }

    fun createTables() = executeTablesScript("scope_tables.create.sql")
    fun dropTables() = executeTablesScript("scope_tables.drop.sql")
}