package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity
import com.github.ol_loginov.heaplibweb.services.proxies.FieldValueProxy.Companion.getValueObject
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass

class JavaClassProxy(
    private val entity: JavaClassEntity,
    private val scope: HeapScope
) : JavaClass {

    companion object {
        @JvmStatic
        fun wrap(other: JavaClassEntity, scope: HeapScope): JavaClass = JavaClassProxy(other, scope)
    }

    private fun wrap(other: JavaClassEntity): JavaClass = wrap(other, scope)

    override fun getAllInstancesSize(): Long = entity.allInstancesSize
    override fun isArray(): Boolean = entity.array
    override fun getName(): String = entity.name
    override fun getInstanceSize() = entity.instanceSize
    override fun getInstancesCount(): Int = entity.instancesCount
    override fun getRetainedSizeByClass(): Long = entity.retainedSizeByClass
    override fun getJavaClassId(): Long = entity.javaClassId
    override fun getSuperClass(): JavaClass? {
        return entity.superClassId
            ?.let { scope.javaClasses.findByIdSure(it) }
            ?.let { wrap(it) }
    }

    override fun getSubClasses(): Collection<JavaClass> = scope
        .javaClasses.streamAllBySuperClassId(entity.javaClassId)
        .map(this::wrap)
        .toList()

    override fun getFields(): List<Field> = scope
        .fields.streamAllByDeclaringClassId(entity.javaClassId)
        .map { FieldProxy(it, scope) }
        .toList()


    override fun getInstances(): List<Instance> = scope
        .instances.streamAllByJavaClassId(entity.javaClassId)
        .map { InstanceProxy.wrap(it, scope) }
        .toList()


    override fun getStaticFieldValues(): List<FieldValue> {
        return scope.fieldValues.streamStaticFieldValues(entity.javaClassId)
            .map { FieldValueProxy.wrap(it, scope) }
            .toList()
    }

    override fun getValueOfStaticField(name: String): Any? = scope
        .fieldValues.findStaticByClassAndFieldName(entity.javaClassId, name)
        ?.let { getValueObject(it, scope) }

    override fun getClassLoader(): Instance? = getValueOfStaticField("<classLoader>") as Instance?
    override fun getInstancesIterator(): Iterator<Instance> = throw UnsupportedOperationException()
}