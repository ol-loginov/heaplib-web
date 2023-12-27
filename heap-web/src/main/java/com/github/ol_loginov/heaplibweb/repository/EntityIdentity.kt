package com.github.ol_loginov.heaplibweb.repository

abstract class EntityIdentity(var id: Int = 0) : EntityInstance {
    override fun hashCode(): Int = javaClass.name.hashCode() * 31 + id
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is EntityIdentity && javaClass == other.javaClass) {
            if (id > 0 || other.id > 0) {
                return id == other.id
            }
        }
        return false
    }


    override fun toString(): String = idString()

    protected fun idString(): String {
        return javaClass.simpleName + "#" + id
    }
}
