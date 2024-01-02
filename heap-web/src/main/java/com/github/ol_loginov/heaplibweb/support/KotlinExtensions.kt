package com.github.ol_loginov.heaplibweb.support

import java.text.NumberFormat

private val prettyNumber = NumberFormat.getNumberInstance().also {
    it.isGroupingUsed = true
}

fun <R> Array<out AutoCloseable>.use(block: () -> R): R {
    try {
        return block()
    } finally {
        this.forEach { it.close() }
    }
}

fun Int.pretty(): String = prettyNumber.format(this)
fun Long.pretty(): String = prettyNumber.format(this)
fun Float.pretty(): String = prettyNumber.format(this)
fun Double.pretty(): String = prettyNumber.format(this)