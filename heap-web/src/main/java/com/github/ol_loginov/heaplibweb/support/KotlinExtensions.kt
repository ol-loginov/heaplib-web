package com.github.ol_loginov.heaplibweb.support

fun <R> Array<out AutoCloseable>.use(block: () -> R): R {
    try {
        return block()
    } finally {
        this.forEach { it.close() }
    }
}