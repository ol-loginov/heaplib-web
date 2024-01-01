package com.github.ol_loginov.heaplibweb.hprof

data class LoadClassRecord(val serialNumber: UInt, val objectId: ULong, val stackTraceSN: UInt, val nameId: ULong, val name: String = "")
