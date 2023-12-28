package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class InstanceEntity(
    var instanceId: Long,
    val instanceNumber: Int,
    val javaClassId: Long,
    val gcRoot: Boolean,
    val size: Long,
    val retainedSize: Long? = null,
    val reachableSize: Long? = null
) : EntityInstance {
}