package com.github.ol_loginov.heaplibweb.hprof

import java.util.concurrent.atomic.AtomicLong

class PeriodicLogger(private val logPeriodMs: Long, private var enabled: Boolean = true) : (Runnable) -> Unit {
    private val lastLog = AtomicLong(0L)

    override fun invoke(runnable: Runnable) {
        if (enabled && lastLog.get() + logPeriodMs < System.currentTimeMillis()) {
            runnable.run()
            lastLog.set(System.currentTimeMillis())
        }
    }
}