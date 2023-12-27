package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class FieldValueEntity(
    var javaClassId: Long,
    val definingInstanceId: Long,
    val fieldId: Int,
    val staticFlag: Boolean,
    val value: String,
    val valueInstanceId: Long? = null
) : EntityInstance {
    internal constructor() : this(0, 0, 0, false, "", null)
}