package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface InstanceRepository extends ListCrudRepository<InstanceEntity, InstanceEntity.PK> {
	Stream<InstanceEntity> streamAllByHeapIdAndJavaClassId(int heapId, long javaClassId);
}
