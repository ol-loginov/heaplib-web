package com.github.ol_loginov.heaplibweb.support

import com.github.ol_loginov.heaplibweb.repository.heap.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.deleteExisting

class WriterExtensionsTests {
    @Test
    fun appendSomeString() {
        val e = FieldValueEntity(34356597872, 3382, 0)

        val backed = ByteArrayOutputStream()
        BufferedWriter(OutputStreamWriter(backed, Charsets.UTF_8))
            .append(e.instanceId).append(TAB)
            .append(e.fieldId).append(TAB)
            .appendMysqlQuoted("20").append(TAB)
            .append(e.valueInstanceId).append(NEWLINE)
            .flush()
        assertThat(backed.toString(Charsets.UTF_8)).isEqualTo("34356597872\t3382\t\"20\"\t0\n")

        val dataFile = File.createTempFile("insert-field-value-", ".tsv").absoluteFile.toPath()
        Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8).use { writer ->
            writer
                .append(e.instanceId).append(TAB)
                .append(e.fieldId).append(TAB)
                .appendMysqlQuoted("20").append(TAB)
                .append(e.valueInstanceId).append(NEWLINE)
        }
        dataFile.deleteExisting()
    }
}