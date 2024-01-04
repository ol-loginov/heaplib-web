package com.github.ol_loginov.heaplibweb.services.loaders

import com.github.ol_loginov.heaplibweb.hprof.HeapRootVisitor
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import kotlin.experimental.and

class JavaRootCollector : HeapRootVisitor {
    val objectRootFlags = mutableMapOf<ULong, Short>()

    override fun onRootUnknown(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_UNKNOWN) { a, b -> a and b }
    }

    override fun onRootJniGlobal(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_JNI_GLOBAL) { a, b -> a and b }
    }

    override fun onRootJniLocal(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_JNI_LOCAL) { a, b -> a and b }
    }

    override fun onRootThreadObject(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_THREAD_OBJECT) { a, b -> a and b }
    }

    override fun onRootJavaFrame(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_JAVA_FRAME) { a, b -> a and b }
    }

    override fun onRootStickyClass(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_STICKY_CLASS) { a, b -> a and b }
    }

    override fun onRootNativeStack(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_NATIVE_STACK) { a, b -> a and b }
    }

    override fun onRootThreadBlock(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_THREAD_BLOCK) { a, b -> a and b }
    }

    override fun onRootMonitorUsed(objectId: ULong) {
        objectRootFlags.merge(objectId, InstanceEntity.ROOT_MONITOR_USED) { a, b -> a and b }
    }
}
