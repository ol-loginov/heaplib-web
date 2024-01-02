package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class InstanceEntity(
    var instanceId: Long,
    val instanceNumber: Int,
    val javaClassId: Long,
    val rootTag: Short,
    val size: Long,
    val arrayTypeTag: Byte = 0,
    val arrayLength: Int = 0,
    val retainedSize: Long? = null,
    val reachableSize: Long? = null
) : EntityInstance {
    companion object {
        const val ROOT_UNKNOWN = (1 shl 0).toShort()
        const val ROOT_JNI_GLOBAL = (1 shl 1).toShort()
        const val ROOT_JNI_LOCAL = (1 shl 2).toShort()
        const val ROOT_THREAD_OBJECT = (1 shl 3).toShort()
        const val ROOT_JAVA_FRAME = (1 shl 4).toShort()
        const val ROOT_STICKY_CLASS = (1 shl 5).toShort()
        const val ROOT_NATIVE_STACK = (1 shl 6).toShort()
        const val ROOT_THREAD_BLOCK = (1 shl 7).toShort()
        const val ROOT_MONITOR_USED = (1 shl 8).toShort()
    }
}