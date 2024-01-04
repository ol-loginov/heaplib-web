package com.github.ol_loginov.heaplibweb.repository.heap

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

interface ObjectArrayLoader {
    fun persistAll(entities: List<ObjectArrayEntity>)
}

internal class ObjectArrayLoaderByInsert(
    private val jdbc: ScopedJdbcClient
) : ObjectArrayLoader {
    private fun persistQueryParameters(entity: ObjectArrayEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "itemIndex" to entity.itemIndex,
        "itemInstanceId" to entity.itemInstanceId
    )

    override fun persistAll(entities: List<ObjectArrayEntity>) {
        MultiValuesInsert(jdbc, "ObjectArray").execute(entities.map { persistQueryParameters(it) })
    }
}

internal class ObjectArrayLoaderByLocalFile(
    private val workingFolder: Path,
    private val jdbc: ScopedJdbcClient
) : ObjectArrayLoader {
    override fun persistAll(entities: List<ObjectArrayEntity>) {
        val dataFile = File.createTempFile("insert-object-array-", ".tsv", workingFolder.toFile()).absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            entities.forEach { e ->
                writer
                    .append(e.instanceId).append(TAB)
                    .append(e.itemIndex).append(TAB)
                    .append(e.itemInstanceId).append(NEWLINE)
            }
        }

        jdbc
            .sql(
                """
            LOAD DATA LOCAL INFILE '$dataFile' INTO TABLE ObjectArray 
                FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
                LINES TERMINATED BY '\n'
                (instanceId,itemIndex,itemInstanceId)
"""
            )
            .update()

        dataFile.deleteExisting()
    }
}