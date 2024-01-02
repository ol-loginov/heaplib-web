package com.github.ol_loginov.heaplibweb.services

import com.github.ol_loginov.heaplibweb.OQLResults
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(NetbeansOQLEngineForTest::class.java)

class NetbeansOQLEngineForTest(heap: Heap) : OQLEngine(heap) {
    override fun executeQuery(query: String, visitor: ObjectVisitor) {
        log.info("Execute >>> $query")
        super.executeQuery(query, visitor)
    }

    fun executeQuery(query: String) {
        log.info("Execute >>> $query")
        super.executeQuery(query, null)
    }

    fun executeQueryAll(query: String, visitor: (o: Any?) -> Unit) {
        this.executeQuery(query) { o ->
            visitor(o)
            false
        }
    }

    fun executeQueryOnce(query: String, visitor: (o: Any?) -> Unit) {
        this.executeQuery(query) { o ->
            visitor(o)
            true
        }
    }

    fun collectQueryAll(query: String): List<Any?> {
        val collector = OQLResults.all()
        this.executeQuery(query, collector)
        return collector.results
    }
}
