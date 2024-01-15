package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.services.ValueNotReadyException
import com.github.ol_loginov.heaplibweb.services.proxies.FieldValueProxy.Companion.getValueObject
import org.netbeans.lib.profiler.heap.Field
import org.netbeans.lib.profiler.heap.FieldValue
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass

class JavaClassProxy(
    private val entity: ClassEntity,
    private val heapRepositories: HeapRepositories
) : JavaClass {

    companion object {
        @JvmStatic
        fun wrap(other: ClassEntity, heapRepositories: HeapRepositories): JavaClass = JavaClassProxy(other, heapRepositories)
    }

    private fun wrap(other: ClassEntity): JavaClass = wrap(other, heapRepositories)

    override fun getAllInstancesSize(): Long = entity.allInstancesSize ?: throw ValueNotReadyException()
    override fun isArray(): Boolean = entity.array
    override fun getName(): String = entity.name
    override fun getInstanceSize() = entity.instanceSize
    override fun getInstancesCount(): Int = entity.instancesCount
    override fun getRetainedSizeByClass(): Long = entity.retainedSizeByClass ?: throw ValueNotReadyException()
    override fun getJavaClassId(): Long = entity.id
    override fun getSuperClass(): JavaClass? {
        return entity.superClassId
            ?.let { heapRepositories.classes.findByIdSure(it) }
            ?.let { wrap(it) }
    }

    override fun getSubClasses(): Collection<JavaClass> = heapRepositories
        .classes.streamAllBySuperClassId(entity.id)
        .map(this::wrap)
        .toList()

    override fun getFields(): List<Field> = heapRepositories
        .fields.findAllByDeclaringClassIdOrderById(entity.id)
        .map { FieldProxy(it, heapRepositories) }

    override fun getInstances(): List<Instance> = heapRepositories
        .instances.streamAllByJavaClassId(entity.id)
        .map { InstanceProxy.wrap(it, heapRepositories) }
        .toList()


    override fun getStaticFieldValues(): List<FieldValue> {
        return heapRepositories.fieldValues.streamStaticFieldValues(entity.id)
            .map { FieldValueProxy.wrap(it, heapRepositories) }
            .toList()
    }

    override fun getValueOfStaticField(name: String): Any? = heapRepositories
        .fieldValues.findStaticByClassAndFieldName(entity.id, name)
        ?.let { getValueObject(it, heapRepositories) }

    override fun getClassLoader(): Instance? = getValueOfStaticField("<classLoader>") as Instance?
    override fun getInstancesIterator(): Iterator<Instance> = throw UnsupportedOperationException()
}
