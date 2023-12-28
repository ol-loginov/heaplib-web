package com.github.ol_loginov.heaplibweb.services.proxies

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity
import org.netbeans.lib.profiler.heap.GCRoot
import org.netbeans.lib.profiler.heap.Heap
import org.netbeans.lib.profiler.heap.HeapSummary
import org.netbeans.lib.profiler.heap.Instance
import org.netbeans.lib.profiler.heap.JavaClass
import java.util.Properties

class HeapProxy(
    private val scope: HeapScope
) : Heap {
    private fun proxyJavaClass(e: JavaClassEntity): JavaClass = JavaClassProxy(e, scope)

    override fun isWriteable(): Boolean = false

    override fun getAllClasses(): List<JavaClass> = scope
        .javaClasses.streamAll()
        .map { proxyJavaClass(it) }
        .toList()


    override fun getJavaClassByID(javaClassId: Long): JavaClass? = scope
        .javaClasses.findById(javaClassId)
        ?.let { proxyJavaClass(it) }

    override fun getJavaClassByName(fqn: String): JavaClass? = scope
        .javaClasses.findByName(fqn)
        ?.let { proxyJavaClass(it) }

    override fun getJavaClassesByRegExp(regexp: String): Collection<JavaClass> = scope
        .javaClasses.findAllByNameRegex(regexp)
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