package com.github.ol_loginov.heaplibweb.repository

import java.time.Instant

class HeapFile private constructor(
    id: Int,
    var relativePath: String,
    var status: HeapFileStatus,
    var loadStart: Instant,
    var loadFinish: Instant,
    var loadProgress: Float,
    var loadMessage: String,
    var loadError: String?
) : EntityIdentity(id) {
    private constructor() : this(0, "", HeapFileStatus.PENDING, Instant.now(), Instant.now(), 0f, "", null)
    constructor(relativePath: String) : this(0, relativePath, HeapFileStatus.PENDING, Instant.now(), Instant.now(), 0f, "", null)
}
