package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityInstance

class NameEntity private constructor(
    var id: Int,
    var name: String
) : EntityInstance {
    constructor(name: String) : this(0, name)
    internal constructor() : this(0, "")
}