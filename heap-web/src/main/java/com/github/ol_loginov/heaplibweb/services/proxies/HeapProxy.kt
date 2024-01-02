package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import org.netbeans.lib.profiler.heap.*
import java.util.*

class HeapProxy(
    private val scope: HeapScope
) : Heap {
    private fun proxyJavaClass(e: ClassEntity): JavaClass = JavaClassProxy(e, scope)

    override fun isWriteable(): Boolean = false

    override fun getAllClasses(): List<JavaClass> = scope
        .classes.streamAll()
        .map { proxyJavaClass(it) }
        .toList()


    override fun getJavaClassByID(javaClassId: Long): JavaClass? = scope
        .classes.findById(javaClassId)
        ?.let { proxyJavaClass(it) }

    override fun getJavaClassByName(fqn: String): JavaClass? = scope
        .classes.findByName(fqn)
        ?.let { proxyJavaClass(it) }

    override fun getJavaClassesByRegExp(regexp: String): Collection<JavaClass> = scope
        .classes.findAllByNameRegex(regexp)
        .map { proxyJavaClass(it) }
        .toList()

    override fun getAllInstances(): Iterable<Instance> = throw UnsupportedOperationException()
    override fun getAllInstances(instanceID: Long): Iterable<Instance> = throw UnsupportedOperationException()
    override fun getBiggestObjectsByRetainedSize(number: Int): List<Instance> = throw UnsupportedOperationException()
    override fun getGCRoot(instance: Instance): GCRoot = throw UnsupportedOperationException()
    override fun getGCRoots(): Collection<GCRoot> = throw UnsupportedOperationException()
    override fun getInstanceByID(instanceId: Long): Instance = throw UnsupportedOperationException()
    override fun getAllInstancesIterator(): Iterator<Instance> = throw UnsupportedOperationException()
    override fun getSummary(): HeapSummary = throw UnsupportedOperationException()
    override fun getSystemProperties(): Properties = throw UnsupportedOperationException()
    override fun isRetainedSizeComputed(): Boolean = throw UnsupportedOperationException()
    override fun isRetainedSizeByClassComputed(): Boolean = throw UnsupportedOperationException()
}