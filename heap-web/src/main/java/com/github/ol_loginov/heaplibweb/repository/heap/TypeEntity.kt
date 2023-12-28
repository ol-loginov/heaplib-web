package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity

class TypeEntity private constructor(
    id: Int,
    var name: String
) : EntityIdentity(id) {
    internal constructor() : this(0, "")
    constructor(name: String) : this(0, name)
}