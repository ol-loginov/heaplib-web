package com.github.ol_loginov.heaplibweb.repository.heap

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

interface InstanceLoader {
    fun persistAll(entities: List<InstanceEntity>)
}

class InstanceLoaderByInsert(
    private val jdbc: ScopedJdbcClient
) : InstanceLoader {
    private fun persistQueryParameters(entity: InstanceEntity) = mapOf(
        "instanceId" to entity.instanceId,
        "fo" to entity.fo,
        "instanceNumber" to entity.instanceNumber,
        "javaClassId" to entity.javaClassId,
        "rootTag" to entity.rootTag,
        "size" to entity.size,
        "arrayTypeTag" to entity.arrayTypeTag,
        "arrayLength" to entity.arrayLength,
        "retainedSize" to entity.retainedSize,
        "reachableSize" to entity.reachableSize
    )

    override fun persistAll(entities: List<InstanceEntity>) {
        MultiValuesInsert(jdbc, "Instance").execute(entities.map { persistQueryParameters(it) })
    }
}

class InstanceLoaderByLocalFile(
    private val workingFolder: Path,
    private val jdbc: ScopedJdbcClient
) : InstanceLoader {
    override fun persistAll(entities: List<InstanceEntity>) {
        val dataFile = File.createTempFile("insert-instance-", ".tsv", workingFolder.toFile()).absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            entities.forEach { e ->
                writer
                    .append(e.instanceId).append(TAB)
                    .append(e.fo).append(TAB)
                    .append(e.instanceNumber).append(TAB)
                    .append(e.javaClassId).append(TAB)
                    .append(e.rootTag).append(TAB)
                    .append(e.size).append(TAB)
                    .append(e.arrayTypeTag).append(TAB)
                    .append(e.arrayLength).append(TAB)
                    .append(e.retainedSize).append(TAB)
                    .append(e.reachableSize).append(NEWLINE)
            }
        }

        jdbc
            .sql(
                """
            LOAD DATA LOCAL INFILE '$dataFile' INTO TABLE Instance 
                FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
                LINES TERMINATED BY '\n'
                (instanceId,fo,instanceNumber,javaClassId,rootTag,size,arrayTypeTag,arrayLength,@retainedSize,@reachableSize)
                SET 
                    retainedSize = NULLIF(@retainedSize,''),
                    reachableSize = NULLIF(@reachableSize,'')
"""
            )
            .update()

        dataFile.deleteExisting()
    }
}