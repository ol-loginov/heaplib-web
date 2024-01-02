package com.github.ol_loginov.heaplibweb.repository.heap

class PrimitiveArrayEntity constructor(
    var instanceId: Long,
    var itemIndex: Int,
    var itemValue: String
) {
    private constructor() : this(0, 0, "")
}