package com.github.ol_loginov.heaplibweb.services.reports

import com.github.ol_loginov.heaplibweb.hprof.HprofValueType
import com.github.ol_loginov.heaplibweb.repository.heap.ClassEntity
import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity
import com.github.ol_loginov.heaplibweb.services.loaders.InsertCollector
import org.springframework.transaction.support.TransactionOperations

class CalculateRetainedSize(
    private val heapRepositories: HeapRepositories,
    private val skipReferences: Boolean = true
) : ReportBuilder {
    private var referentField: FieldEntity? = null

    override fun invoke(transactionOperations: TransactionOperations) {
        referentField = transactionOperations.execute {
            heapRepositories
                .classes.findByName("java.lang.ref.Reference")
                ?.let { heapRepositories.fields.findByDeclaringClassIdAndName(it.id, "referent") }
        }

        val fieldCache = mutableMapOf<Long, ClassFields>()
        transactionOperations.execute {
            heapRepositories.classes.streamAll().use { classStream ->
                classStream.forEach { resolveClassFields(it, fieldCache) }
            }
        }

        val retainedSizeUpdate = InsertCollector<Pair<Long, Long>>("pojo instance retained size") { list ->
            transactionOperations.executeWithoutResult {
                list.forEach { item ->
                    heapRepositories.instances.updateRetainedSizeByClass(item.first, item.second)
                }
            }
        }

        retainedSizeUpdate.use {
            fieldCache.values.asSequence()
                .filter { it.referenceFields.isEmpty() }
                .forEach { retainedSizeUpdate(it.declaringClassId to it.instanceSize.toLong()) }
        }

        val roots = mutableListOf<Long>()
        roots.addAll(heapRepositories.instances.findAllRootInstances())
//        while (roots.isNotEmpty()) {
//            calculateRetainedSize(roots.removeAt(0), fieldCache)
//        }
    }

    private fun resolveClassFields(classEntity: ClassEntity, fieldCache: MutableMap<Long, ClassFields>) {
        if (fieldCache.containsKey(classEntity.id)) return

        val resolveStack = mutableListOf<ClassEntity>()
        resolveStack.add(classEntity)
        while (resolveStack.last().superClassId > 0) {
            val superClass = resolveStack.last().superClassId
            if (fieldCache.containsKey(superClass)) {
                break
            }
            resolveStack.add(heapRepositories.classes.findById(superClass)!!)
        }

        while (resolveStack.isNotEmpty()) {
            val last = resolveStack.removeLast()

            val referenceFields = heapRepositories
                .fields.findAllByDeclaringClassIdOrderById(last.id)
                .filter { !it.staticFlag && it.type == HprofValueType.Object }
                .map { it.id }
                .toTypedArray()
            val superClass = fieldCache[last.superClassId]
            val currentClass = ClassFields(
                last.id,
                last.instanceSize + (superClass?.instanceSize ?: 0),
                referenceFields + (superClass?.referenceFields ?: emptyArray())
            )
            fieldCache[last.id] = currentClass
        }
    }

    private fun calculateRetainedSize(instanceId: Long, fieldCache: HashMap<Int, FieldEntity>): InstanceEntity {
        val instance = loadInstance(instanceId)
        if (instance.retainedSize != null) {
            return instance
        }

        return instance
    }

    private fun loadInstance(instanceId: Long): InstanceEntity = heapRepositories.instances.findById(instanceId)!!
    /*
        fun getRetainedSet(objSet: MutableMap<Long, InstanceEntity>, fieldCache: HashMap<Int, FieldEntity>): Set<Instance> {
    
            val results = HashSet<InstanceEntity>()
            val roots = heapRepositories.instances.findAllRoots()
            val marked = HashSet<Long>()
            val fifo = ArrayDeque<InstanceEntity>()
            for (r in roots) {
                val curr = r.instanceId
                if (!objSet.containsKey(curr)) {
                    fifo.add(r)
                }
            }
    
            while (!fifo.isEmpty()) {
                val curr = fifo.removeFirst()
                if (!marked.add(curr.instanceId)) {
                    continue
                }
    
                for (fv in heapRepositories.fieldValues.findAllReferenceValuesByInstance(curr.instanceId)) {
                    // skip weak references
                    if (skipWeakReferences && fv.fieldId == ref?.id) {
                        continue
                    }
    
                    if (fv.valueInstanceId > 0) {
                        val fieldValue = loadInstance(fv.valueInstanceId)
                        val field = fieldCache.computeIfAbsent(fv.fieldId) { heapRepositories.fields.findById(it)!! }
                        when (HprofValueType.valueOf(field.typeTag)) {
                            HprofValueType.Object -> {
                                if (!objSet.containsKey(fieldValue.instanceId)) {
                                    fifo.add(fieldValue)
                                }
                            }
    
                            HprofValueType.Array -> {
                                when (HprofValueType.valueOf(fieldValue.arrayTypeTag)) {
                                    HprofValueType.Array,
                                    HprofValueType.Object -> {
                                        heapRepositories.objectArrayItems.streamNonNullItems(fieldValue.instanceId).use { stream ->
                                            stream.forEach {
                                                if (!objSet.contains(it.itemInstanceId)) {
                                                    fifo.add(loadInstance(it.itemInstanceId))
                                                }
                                            }
                                        }
                                    }
    
                                    else -> {}
                                }
                            }
    
                            else -> {}
                        }
                    }
                }
            }
    
            // now find what we can reach from 'in'
            fifo.addAll(objSet.values)
            results.addAll(objSet.values)
            while (!fifo.isEmpty()) {
                val curr = fifo.removeFirst()
                for (fv in heapRepositories.fieldValues.findAllReferenceValuesByInstance(curr.instanceId)) {
                    // skip weak references
                    if (skipWeakReferences && fv.fieldId == ref?.id) {
                        continue
                    }
    
                    // 
                    if (fv is ObjectFieldValue) {
                        val neu = fv.instance
                        if (neu != null && !marked.contains(neu)) {
                            if (results.add(neu)) {
                                fifo.add(neu)
                            }
                        }
                    }
                }
            }
            return results
        }
        
     */

    data class ClassFields(val declaringClassId: Long, val instanceSize: Int, val referenceFields: Array<Int>)
}