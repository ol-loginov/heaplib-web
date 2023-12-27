package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity
import org.springframework.lang.NonNull

class FieldEntity(
    id: Int,
    /**
     * @see JavaClassEntity
     */
    var declaringClassId: Long,
    var name: String,
    var staticFlag: Boolean,
    /**
     * Might be JavaClassEntity or internal primitive id
     */
    var typeId: Int
) : EntityIdentity(id) {
    internal constructor() : this(0, 0, "", false, 0)

    @NonNull
    override fun toString() = super.idString() + " (class=" + declaringClassId + ",name=" + name + ")"
}