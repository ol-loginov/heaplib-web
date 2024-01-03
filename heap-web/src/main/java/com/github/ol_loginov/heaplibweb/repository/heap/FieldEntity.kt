package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.EntityIdentity
import org.springframework.lang.NonNull

class FieldEntity private constructor(
    id: Int,
    var declaringClassId: Long,
    var nameId: Int,
    var staticFlag: Boolean,
    var typeTag: Byte
) : EntityIdentity(id) {
    constructor(declaringClassId: Long, nameId: Int, staticFlag: Boolean, typeTag: Byte) : this(0, declaringClassId, nameId, staticFlag, typeTag)
    private constructor() : this(0, 0, 0, false, 0)

    val type
        get() = HprofValueType.valueOf(typeTag.toUByte())

    @NonNull
    override fun toString() = "${super.idString()} (class=$declaringClassId,name=$nameId)"
}