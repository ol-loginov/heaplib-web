package com.github.ol_loginov.heaplibweb.oql

class OQLEngineForTest : OQLEngine() {
    fun executeQueryAll(query: String, receiver: (Any?) -> Unit) {
        executeForStream(query).forEach(receiver)
    }

    fun executeQueryUnless(query: String, receiver: (Any?) -> Boolean) {
        executeForStream(query).forEach {
            if (receiver(it))
                return@forEach
        }
    }

    fun executeQueryOnce(query: String, receiver: (Any?) -> Unit) {
        executeQueryUnless(query) {
            receiver(it)
            true
        }
    }

    fun executeQuery(query: String) {
        executeForStream(query).count()
    }
}