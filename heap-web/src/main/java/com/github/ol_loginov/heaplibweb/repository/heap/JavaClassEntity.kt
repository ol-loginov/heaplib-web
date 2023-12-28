package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance
import org.springframework.lang.NonNull

class JavaClassEntity(
    var javaClassId: Long,
    
    var name: String,
    var allInstancesSize: Long,
    var array: Boolean,
    var instanceSize: Int,
    var instancesCount: Int,
    var retainedSizeByClass: Long,
    var superClassId: Long?
) : EntityInstance {
    internal constructor() : this(0, "", 0, false, 0, 0, 0, null)

    @NonNull
    override fun toString() = "${javaClass.simpleName}#$javaClassId"
}