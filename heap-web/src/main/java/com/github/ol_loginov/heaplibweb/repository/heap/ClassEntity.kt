package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance
import org.springframework.lang.NonNull

class ClassEntity(
    var id: Long,
    var superClassId: Long,
    var classLoaderObjectId: Long,

    var name: String,
    var allInstancesSize: Long?,
    var array: Boolean,
    var instanceSize: Int,
    var instancesCount: Int,
    var retainedSizeByClass: Long? = null
) : EntityInstance {
    internal constructor() : this(0, 0, 0, "", null, false, 0, 0)

    @NonNull
    override fun toString() = "${javaClass.simpleName}#$id"
}