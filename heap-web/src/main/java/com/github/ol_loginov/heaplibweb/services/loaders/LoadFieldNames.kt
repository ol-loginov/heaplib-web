package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.ClassDump
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.repository.heap.NameEntity
import com.github.ol_loginov.heaplibweb.support.pretty
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionOperations

private val log = LoggerFactory.getLogger(LoadFieldNames::class.java)

internal class LoadFieldNames(
    private val transactionOperations: TransactionOperations,
    private val heapRepositories: HeapRepositories,
    private val classDumpLookup: ClassDumpLookup,
    private val fieldNameLookup: FieldNameLookup
) : Task {
    private var passed = 0
    private var classCount = 0
    private var names = 0

    override fun getText(): String = "import field names: ${passed.pretty()}/${classCount.pretty()} classes (names=${names.pretty()})"

    override fun run(callback: Task.Callback) {
        classCount = classDumpLookup.classCount
        passed = 0

        callback.saveProgress(this, true)

        val insert = InsertCollector("fields") { list ->
            transactionOperations.executeWithoutResult { _ ->
                heapRepositories.names.persistAll(list)
            }
        }

        val knownNames = mutableSetOf<String>()
        insert.use {
            classDumpLookup.classes.forEach { classDump ->
                recordFieldNames(classDump, knownNames, insert)
                passed++
                callback.saveProgress(this)
            }
        }

        log.info("refresh names")
        transactionOperations.executeWithoutResult { _ ->
            fieldNameLookup.refresh(heapRepositories)
        }

        callback.saveProgress(this, true)
    }

    private fun recordFieldNames(classDump: ClassDump, knownNames: MutableSet<String>, insert: InsertCollector<NameEntity>) {
        classDump.staticFields.asSequence()
            .map { it.name.orEmpty() }
            .filter { knownNames.add(it) }
            .forEach {
                insert(NameEntity(it))
                names++
            }
        classDump.instanceFields.asSequence()
            .map { it.name.orEmpty() }
            .filter { knownNames.add(it) }
            .forEach {
                insert(NameEntity(it))
                names++
            }
    }
}