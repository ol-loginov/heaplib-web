package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.TypeEntity
import org.netbeans.lib.profiler.heap.Type

class TypeProxy(
    private val entity: TypeEntity
) : Type {
    override fun getName(): String = entity.name
}