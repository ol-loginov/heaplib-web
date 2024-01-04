package com.github.ol_loginov.heaplibweb.repository.heap

class ClassLoaderByInsert(private val jdbc: ScopedJdbcClient) : ClassLoader {
    private fun persistQueryParameters(entity: ClassEntity) = mapOf(
        "id" to entity.id,
        "classLoaderObjectId" to entity.classLoaderObjectId,
        "name" to entity.name,
        "allInstancesSize" to entity.allInstancesSize,
        "array" to entity.array,
        "instanceSize" to entity.instanceSize,
        "instancesCount" to entity.instancesCount,
        "retainedSizeByClass" to entity.retainedSizeByClass,
        "superClassId" to entity.superClassId
    )

    override fun persistAll(entities: List<ClassEntity>) {
        MultiValuesInsert(jdbc, "Class").execute(entities.map { persistQueryParameters(it) })
    }
}