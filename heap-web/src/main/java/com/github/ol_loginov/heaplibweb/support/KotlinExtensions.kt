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

fun Any.void(): Unit {}

fun <A, B> tuple(a: A, b: B) = Pair(a, b)
fun <A, B, C> tuple(a: A, b: B, c: C) = Triple(a, b, c)
fun <A, B, C, D> tuple(a: A, b: B, c: C, d: D) = Quartet(a, b, c, d)

data class Quartet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

open class LazyReset<T>(private val factory: () -> T) : Lazy<T> {
    @Volatile
    private var valueCache: T? = null

    override val value: T
        get() {
            initialize()
            return valueCache!!
        }

    fun initialize() {
        if (!isInitialized()) {
            valueCache = factory()
        }
    }

    fun clear() {
        valueCache = null
    }

    override fun isInitialized(): Boolean = valueCache != null
}
