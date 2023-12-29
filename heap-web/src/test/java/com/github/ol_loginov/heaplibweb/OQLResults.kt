package com.github.ol_loginov.heaplibweb

import org.netbeans.modules.profiler.oql.engine.api.OQLEngine.ObjectVisitor

class OQLResults private constructor(private val stopper: (o: Any?) -> Boolean) : ObjectVisitor {
    companion object {
        fun all() = OQLResults { false }
    }

    val results = mutableListOf<Any?>()

    override fun visit(o: Any?): Boolean {
        results.add(o)
        return stopper(o)
    }
}