package com.github.ol_loginov.heaplibweb.services.loaders

interface Task {
    fun getText(): String

    fun run(callback: Callback)

    interface Callback {
        fun saveProgress(task: Task, force: Boolean = false)
    }
}