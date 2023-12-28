package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import java.time.Instant

class HeapEntity private constructor(
    id: Int,
    var fileId: Int,
    var tm: Instant = Instant.now()
) : EntityIdentity(id) {
    constructor(file: HeapFile) : this(0, file.id)
    private constructor() : this(0, 0)
}