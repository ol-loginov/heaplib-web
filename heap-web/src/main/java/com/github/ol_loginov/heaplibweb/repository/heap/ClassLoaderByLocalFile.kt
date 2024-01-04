package com.github.ol_loginov.heaplibweb.repository.heap

import java.io.File
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting

const val OFD = '\t'
const val NEWLINE = '\n'

fun Writer.append(n: Int): Writer = this.append(n.toString())
fun Writer.append(n: Long): Writer = this.append(n.toString())
fun Writer.append(n: Long?): Writer = if (n == null) this else this.append(n)
fun Writer.append(n: Boolean): Writer = if (n) this.append('1') else this.append('0')
fun Writer.append(n: Boolean?): Writer = if (n == null) this else this.append(n)

class ClassLoaderByLocalFile(
    private val workingFolder: Path,
    private val jdbc: ScopedJdbcClient
) : ClassLoader {
    override fun persistAll(entities: List<ClassEntity>) {
        val dataFile = File.createTempFile("insert-class-", ".tsv", workingFolder.toFile()).absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            entities.forEach { e ->
                writer
                    .append(e.id).append(OFD)
                    .append(e.classLoaderObjectId).append(OFD)
                    .append(e.name).append(OFD)
                    .append(e.allInstancesSize).append(OFD)
                    .append(e.array).append(OFD)
                    .append(e.instanceSize).append(OFD)
                    .append(e.instancesCount).append(OFD)
                    .append(e.retainedSizeByClass).append(OFD)
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