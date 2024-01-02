package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofStream
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.support.pretty
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

internal class LoadJavaClasses(
    private val heap: HprofStream,
    private val transactionOperations: TransactionOperations,
    private val scope: HeapScope,
    private val classDumpLookup: ClassDumpLookup
) : Task {
    companion object {
        private val log = LoggerFactory.getLogger(LoadJavaClasses::class.java)
        private val ARRAY_JVM_PREFIX = Regex("^\\[+[ZCFDBSIJL]")
        fun nullIfZero(long: Long): Long? = if (long == 0L) null else long
    }

    private var passed: Long = 0

    @Volatile
    private var classCount: Int = -1

    override fun getText(): String = "import java classes: ${passed.pretty()}/${classCount.pretty()} classes"

    override fun run(callback: Task.Callback) {
        passed = 0

        callback.saveProgress(this, true)
        val insert = InsertCollector("classes") { list ->
            transactionOperations.executeWithoutResult {
                scope.classes.persistAll(list)
            }
        }

        insert.use {
            val classes = heap.scanClasses()
            classDumpLookup.putAll(classes)
            classCount = classDumpLookup.classCount

            classes.forEach { loadClass: ClassDump ->
                persistJavaClass(loadClass, insert)
                passed++
                callback.saveProgress(this)
            }
        }

        callback.saveProgress(this, true)
    }

    private fun persistJavaClass(clazz: ClassDump, insert: (ClassEntity) -> Unit) {
        if (log.isDebugEnabled) log.debug("{}", clazz.className.name)

        insert(
            ClassEntity(
                clazz.classObjectId.toLong(),
                nullIfZero(clazz.classLoaderObjectId.toLong()),
                unmangleJvmClassName(clazz.className.orEmpty()),
                null,
                null, // clazz.isArray,
                clazz.instanceSize.toInt(),
                0, null, null
            )
        )
    }

    private fun unmangleJvmClassName(className: String): String {
        if (!className.startsWith('[')) return className

        val arrayMatch = ARRAY_JVM_PREFIX.matchAt(className, 0) ?: return className
        val arrayPrefix = arrayMatch.value

        val tail = className.substring(arrayPrefix.length).trimEnd(';')
        val depth = arrayPrefix.count { it == '[' }
        val type = when (arrayPrefix.last()) {
            'J' -> "long"
            'I' -> "int"
            'S' -> "short"
            'B' -> "byte"
            'D' -> "double"
            'F' -> "float"
            'C' -> "char"
            'Z' -> "boolean"
            'L' -> tail
            else -> throw IllegalArgumentException("$arrayPrefix - invalid array prefix")
        }
        return type + "[]".repeat(depth)
    }
}
