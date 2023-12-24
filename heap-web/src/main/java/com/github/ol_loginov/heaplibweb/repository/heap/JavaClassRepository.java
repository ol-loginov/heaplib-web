package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface JavaClassRepository extends CrudRepository<JavaClassEntity, JavaClassEntity.PK> {
	Optional<JavaClassEntity> findByHeapIdAndName(int heapId, String name);

	@Query(value = "select * from JavaClass where heapId = :heapId and regexp_like(name, :nameRegex)", nativeQuery = true)
	Stream<JavaClassEntity> findAllByHeapIdAndNameMatches(int heapId, String nameRegex);

	Stream<JavaClassEntity> streamAllByHeapId(int heapId);

	Stream<JavaClassEntity> streamAllByHeapIdAndSuperClassId(int heapId, long superClassId);
}
