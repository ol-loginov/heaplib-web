package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity
import org.springframework.lang.NonNull

class FieldEntity private constructor(
    id: Int,
    var declaringClassId: Long,
    var name: String,
    var staticFlag: Boolean,
    var typeId: Int
) : EntityIdentity(id) {
    constructor(declaringClassId: Long, name: String, staticFlag: Boolean, typeId: Int) : this(0, declaringClassId, name, staticFlag, typeId)
    private constructor() : this(0, 0, "", false, 0)

    @NonNull
    override fun toString() = super.idString() + " (class=" + declaringClassId + ",name=" + name + ")"
}