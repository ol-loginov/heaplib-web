package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.Type

class FieldProxy(
    private val entity: FieldEntity,
    private val heapRepositories: HeapRepositories
) : Field {
    override fun getDeclaringClass(): JavaClass = JavaClassProxy.wrap(heapRepositories.classes.findByIdSure(entity.declaringClassId), heapRepositories)
    override fun getName() = entity.nameId.toString()
    override fun isStatic() = entity.staticFlag
    override fun getType(): Type? = null
}