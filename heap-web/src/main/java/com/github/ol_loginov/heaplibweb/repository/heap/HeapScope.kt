package com.github.ol_loginov.heaplibweb.repository.heap

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.simple.JdbcClient
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

class HeapScope(
    private val tablePrefix: String,
    private val jdbc: JdbcClient,
    jdbcOperations: NamedParameterJdbcOperations,
    private val insertByLoadLocalFile: Boolean = true
) {
    private val scopedJdbcClient = ScopedJdbcClient(tablePrefix, jdbc, jdbcOperations)
    private val localFileFolder: Path = Paths.get(System.getProperty("java.io.tmpdir"))

    val names: NameRepository by lazy { NameRepositoryImpl(scopedJdbcClient) }
    val classes: ClassRepository by lazy { ClassRepositoryImpl(scopedJdbcClient) }
    val classLoader: ClassLoader by lazy {
        if (insertByLoadLocalFile) ClassLoaderByLocalFile(localFileFolder, scopedJdbcClient)
        else ClassLoaderByInsert(scopedJdbcClient)
    }
    val fields: FieldRepository by lazy { FieldRepositoryImpl(scopedJdbcClient) }
    val fieldValues: FieldValueRepository by lazy { FieldValueRepositoryImpl(scopedJdbcClient) }
    val fieldValueLoader: FieldValueLoader by lazy {
        if (insertByLoadLocalFile) FieldValueLoaderByLocalFile(localFileFolder, scopedJdbcClient)
        else FieldValueLoaderByInsert(scopedJdbcClient)
    }
    val instances: InstanceRepository by lazy { InstanceRepositoryImpl(scopedJdbcClient) }
    val instanceLoader: InstanceLoader by lazy {
        if (insertByLoadLocalFile) InstanceLoaderByLocalFile(localFileFolder, scopedJdbcClient)
        else InstanceLoaderByInsert(scopedJdbcClient)
    }
    val primitiveArrayItems: PrimitiveArrayRepository by lazy { PrimitiveArrayRepositoryImpl(scopedJdbcClient) }
    val objectArrayItems: ObjectArrayRepository by lazy { ObjectArrayRepositoryImpl(scopedJdbcClient) }
    val objectArrayLoader: ObjectArrayLoader by lazy {
        if (insertByLoadLocalFile) ObjectArrayLoaderByLocalFile(localFileFolder, scopedJdbcClient)
        else ObjectArrayLoaderByInsert(scopedJdbcClient)
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