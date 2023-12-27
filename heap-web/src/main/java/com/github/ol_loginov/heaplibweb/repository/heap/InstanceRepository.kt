package com.github.ol_loginov.heaplibweb.repository.heap

import java.util.stream.Stream

interface InstanceRepository {
    fun streamAllByJavaClassId(javaClassId: Long): Stream<InstanceEntity>
}