package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class FieldValueEntity(
    var instanceId: Long,
    var fieldId: Int,
    var value: String,
    var valueInstanceId: Long = 0
) : EntityInstance {
    internal constructor() : this(0, 0, "", 0)
}