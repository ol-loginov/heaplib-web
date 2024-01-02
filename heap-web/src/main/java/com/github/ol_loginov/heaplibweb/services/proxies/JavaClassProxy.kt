package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.services.ValueNotReadyException
import com.github.ol_loginov.heaplibweb.services.proxies.FieldValueProxy.Companion.getValueObject
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass

class JavaClassProxy(
    private val entity: ClassEntity,
    private val scope: HeapScope
) : JavaClass {

    companion object {
        @JvmStatic
        fun wrap(other: ClassEntity, scope: HeapScope): JavaClass = JavaClassProxy(other, scope)
    }

    private fun wrap(other: ClassEntity): JavaClass = wrap(other, scope)

    override fun getAllInstancesSize(): Long = entity.allInstancesSize ?: throw ValueNotReadyException()
    override fun isArray(): Boolean = entity.array ?: throw ValueNotReadyException()
    override fun getName(): String = entity.name
    override fun getInstanceSize() = entity.instanceSize
    override fun getInstancesCount(): Int = entity.instancesCount
    override fun getRetainedSizeByClass(): Long = entity.retainedSizeByClass ?: throw ValueNotReadyException()
    override fun getJavaClassId(): Long = entity.id
    override fun getSuperClass(): JavaClass? {
        return entity.superClassId
            ?.let { scope.classes.findByIdSure(it) }
            ?.let { wrap(it) }
    }

    override fun getSubClasses(): Collection<JavaClass> = scope
        .classes.streamAllBySuperClassId(entity.id)
        .map(this::wrap)
        .toList()

    override fun getFields(): List<Field> = scope
        .fields.findAllByDeclaringClassIdOrderById(entity.id)
        .map { FieldProxy(it, scope) }

    override fun getInstances(): List<Instance> = scope
        .instances.streamAllByJavaClassId(entity.id)
        .map { InstanceProxy.wrap(it, scope) }
        .toList()


    override fun getStaticFieldValues(): List<FieldValue> {
        return scope.fieldValues.streamStaticFieldValues(entity.id)
            .map { FieldValueProxy.wrap(it, scope) }
            .toList()
    }

    override fun getValueOfStaticField(name: String): Any? = scope
        .fieldValues.findStaticByClassAndFieldName(entity.id, name)
        ?.let { getValueObject(it, scope) }

    override fun getClassLoader(): Instance? = getValueOfStaticField("<classLoader>") as Instance?
    override fun getInstancesIterator(): Iterator<Instance> = throw UnsupportedOperationException()
}