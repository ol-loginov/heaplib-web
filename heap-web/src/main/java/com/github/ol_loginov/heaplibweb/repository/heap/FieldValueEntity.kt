package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class FieldValueEntity(
    var javaClassId: Long,
    var definingInstanceId: Long,
    var fieldId: Int,
    var staticFlag: Boolean,
    var value: String,
    var valueInstanceId: Long = 0
) : EntityInstance {
    internal constructor() : this(0, 0, 0, false, "", 0)
}