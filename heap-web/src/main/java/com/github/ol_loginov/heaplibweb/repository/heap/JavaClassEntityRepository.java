package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface JavaClassEntityRepository extends CrudRepository<JavaClassEntity, Integer> {
	Stream<JavaClassEntity> streamAllByHeap(HeapEntity heap);

	Stream<JavaClassEntity> streamAllBySuperClass(JavaClassEntity superClass);
}
