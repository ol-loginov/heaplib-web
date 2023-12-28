package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.JavaClass
import org.netbeans.lib.profiler.heap.Type

class FieldProxy(
    private val entity: FieldEntity,
    private val scope: HeapScope
) : Field {
    override fun getDeclaringClass(): JavaClass = JavaClassProxy.wrap(scope.javaClasses.findByIdSure(entity.declaringClassId), scope)
    override fun getName() = entity.name
    override fun isStatic() = entity.staticFlag
    override fun getType(): Type? = scope.types
        .findById(entity.typeId)
        ?.let { TypeProxy(it) }
}