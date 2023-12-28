package com.github.ol_loginov.heaplibweb.repository

import java.time.Instant
import java.time.ZonedDateTime

class HeapFile private constructor(
    id: Int,
    var path: String,
    var status: HeapFileStatus,
    var loadStart: ZonedDateTime,
    var loadFinish: ZonedDateTime?,
    var loadProgress: Float,
    var loadMessage: String,
    var loadError: String?
) : EntityIdentity(id) {
    private constructor() : this(0, "", HeapFileStatus.PENDING, ZonedDateTime.now(), null, 0f, "", null)
    constructor(path: String) : this(0, path, HeapFileStatus.PENDING, ZonedDateTime.now(), null, 0f, "", null)
}
