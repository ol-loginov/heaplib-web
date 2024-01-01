package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance
import org.springframework.lang.NonNull

class JavaClassEntity(
    var javaClassId: Long,

    var name: String,
    var allInstancesSize: Long?,
    var array: Boolean?,
    var instanceSize: Int,
    var instancesCount: Int?,
    var retainedSizeByClass: Long?,
    var superClassId: Long?
) : EntityInstance {
    internal constructor() : this(0, "", null, null, 0, null, null, null)

    @NonNull
    override fun toString() = "${javaClass.simpleName}#$javaClassId"
}