package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface FieldValueRepository extends ListCrudRepository<FieldValueEntity, FieldValueEntity.PK> {
	Stream<FieldValueEntity> streamByDefiningInstanceId(long instanceId);

	@Query(nativeQuery = true, value = "select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId and F.name = :fieldName")
	Optional<FieldValueEntity> findOneByInstanceAndFieldName(long definingInstanceId, String fieldName);
}
