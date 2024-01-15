package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.hprof.HprofFile
import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.support.pretty
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations

internal class LoadJavaClasses(
    private val hprof: HprofFile,
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
    private val classDumpLookup: ClassDumpLookup
) : Task {
    companion object {
        private val log = LoggerFactory.getLogger(LoadJavaClasses::class.java)
        private val RE_ARRAY_JVM_PREFIX = Regex("^\\[+[ZCFDBSIJL]")
        private val RE_SLASH = Regex("/")
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
                heapRepositories.classLoader.persistAll(list)
            }
        }

        insert.use {
            val classes = hprof.scanClasses()
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
                clazz.superClassObjectId.toLong(),
                clazz.classLoaderObjectId.toLong(),
                unmangleJvmClassName(clazz.className.orEmpty()),
                null,
                clazz.className.orEmpty().startsWith('['), // clazz.isArray,
                clazz.instanceSize,
                0
            )
        )
    }

    private fun unmangleJvmClassName(className: String): String {
        var out = className
        if (className.startsWith('[')) {
            val arrayMatch = RE_ARRAY_JVM_PREFIX.matchAt(className, 0) ?: return className
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
            out = type + "[]".repeat(depth)
        }

        return out.replace(RE_SLASH, ".")
    }
}
