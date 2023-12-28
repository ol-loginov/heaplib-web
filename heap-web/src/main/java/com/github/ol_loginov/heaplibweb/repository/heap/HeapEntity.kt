package com.github.ol_loginov.heaplibweb.repository.heap

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import java.time.ZonedDateTime

class HeapEntity private constructor(
    id: Int,
    var fileId: Int,
    var tm: ZonedDateTime,
    var tablePrefix: String
) : EntityIdentity(id) {
    constructor(file: HeapFile) : this(0, file.id, ZonedDateTime.now(), "")
    private constructor() : this(0, 0, ZonedDateTime.now(), "")

    fun generateTablePrefix() {
        this.tablePrefix = "Z${id}_"
    }
}