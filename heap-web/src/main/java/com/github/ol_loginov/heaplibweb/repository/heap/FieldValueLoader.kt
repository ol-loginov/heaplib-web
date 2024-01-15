package com.github.ol_loginov.heaplibweb.repository.heap

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

interface FieldValueLoader {
    fun persistAll(entities: List<FieldValueEntity>)
}

internal class FieldValueLoaderByInsert(
    private val jdbc: ScopedJdbcClient
) : FieldValueLoader {
    private fun persistQueryParameters(entity: FieldValueEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "fieldId" to entity.fieldId,
        "valueInstanceId" to entity.valueInstanceId
    )

    override fun persistAll(entities: List<FieldValueEntity>) {
        MultiValuesInsert(jdbc, "FieldValue").execute(entities.map { persistQueryParameters(it) })
    }
}

class FieldValueLoaderByLocalFile(
    private val workingFolder: Path,
    private val jdbc: ScopedJdbcClient
) : FieldValueLoader {
    companion object {
        val log = LoggerFactory.getLogger(FieldValueLoaderByLocalFile::class.java)
    }

    override fun persistAll(entities: List<FieldValueEntity>) {
        val dataFile = File.createTempFile("insert-field-value-", ".tsv", workingFolder.toFile()).absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            entities.forEach { e ->
                writer
                    .append(e.instanceId).append(TAB)
                    .append(e.fieldId).append(TAB)
                    .append(e.valueInstanceId).append(NEWLINE)
            }
            writer.flush()
        }

        jdbc
            .sql(
                """
            LOAD DATA LOCAL INFILE '$dataFile' INTO TABLE FieldValue 
                FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
                LINES TERMINATED BY '\n'
                (instanceId,fieldId,valueInstanceId)
"""
            )
            .update()

        dataFile.deleteExisting()
    }
}