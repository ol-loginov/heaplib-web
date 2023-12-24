package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface FieldRepository extends CrudRepository<FieldEntity, Integer> {
	Stream<FieldEntity> streamAllByHeapIdAndDeclaringClassId(int heapId, long declaringClassId);

	Stream<FieldEntity> streamAllByHeapId(int heapId);
}
