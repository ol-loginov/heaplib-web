package com.github.ol_loginov.heaplibweb.repository.heap

import java.io.File
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

interface ClassLoader {
    fun persistAll(entities: List<ClassEntity>)
}

class ClassLoaderByInsert(private val jdbc: ScopedJdbcClient) : ClassLoader {
    private fun persistQueryParameters(entity: ClassEntity) = mapOf(
        "id" to entity.id,
        "classLoaderObjectId" to entity.classLoaderObjectId,
        "name" to entity.name,
        "allInstancesSize" to entity.allInstancesSize,
        "array" to entity.array,
        "instanceSize" to entity.instanceSize,
        "instancesCount" to entity.instancesCount,
        "retainedSizeByClass" to entity.retainedSizeByClass,
        "superClassId" to entity.superClassId
    )

    override fun persistAll(entities: List<ClassEntity>) {
        MultiValuesInsert(jdbc, "Class").execute(entities.map { persistQueryParameters(it) })
    }
}

const val TAB = '\t'
const val NEWLINE = '\n'

fun Writer.append(n: Byte): Writer = append(n.toString())
fun Writer.append(n: Short): Writer = append(n.toString())
fun Writer.append(n: Int): Writer = append(n.toString())
fun Writer.append(n: Long): Writer = append(n.toString())
fun Writer.append(n: Long?): Writer = if (n == null) this else append(n)
fun Writer.append(n: Boolean): Writer = if (n) this.append('1') else append('0')
fun Writer.append(n: Boolean?): Writer = if (n == null) this else append(n)
fun Writer.appendMysqlQuoted(n: String): Writer = append('\"' + n.replace("\"", "\"\"") + '"')

class ClassLoaderByLocalFile(
    private val workingFolder: Path,
    private val jdbc: ScopedJdbcClient
) : ClassLoader {
    override fun persistAll(entities: List<ClassEntity>) {
        val dataFile = File.createTempFile("insert-class-", ".tsv", workingFolder.toFile()).absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            entities.forEach { e ->
                writer
                    .append(e.id).append(TAB)
                    .append(e.classLoaderObjectId).append(TAB)
                    .append(e.name).append(TAB)
                    .append(e.allInstancesSize).append(TAB)
                    .append(e.array).append(TAB)
                    .append(e.instanceSize).append(TAB)
                    .append(e.instancesCount).append(TAB)
                    .append(e.retainedSizeByClass).append(TAB)
                    .append(e.superClassId).append(NEWLINE)
            }
        }

        jdbc
            .sql(
                """
            LOAD DATA LOCAL INFILE '$dataFile' INTO TABLE Class 
                FIELDS TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"'
                LINES TERMINATED BY '\n'
                (id,@classLoaderObjectId,name,allInstancesSize,array,instanceSize,instancesCount,retainedSizeByClass,superClassId)
                SET 
                    classLoaderObjectId = NULLIF(@classLoaderObjectId,'')
"""
            )
            .update()

        dataFile.deleteExisting()
    }
}