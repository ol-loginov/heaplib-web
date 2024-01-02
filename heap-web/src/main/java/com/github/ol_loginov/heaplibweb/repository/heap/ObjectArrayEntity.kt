package com.github.ol_loginov.heaplibweb.repository.heap

class ObjectArrayEntity constructor(
    var instanceId: Long,
    var itemIndex: Int,
    var itemInstanceId: Long
) {
    private constructor() : this(0, 0, 0)
}