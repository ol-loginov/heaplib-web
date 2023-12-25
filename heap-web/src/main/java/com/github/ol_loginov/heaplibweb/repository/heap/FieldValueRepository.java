package com.github.ol_loginov.heaplibweb.repository.heap;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface FieldValueRepository extends ListCrudRepository<FieldValueEntity, FieldValueEntity.PK> {
	@Query(nativeQuery = true, value = "select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId")
	Stream<FieldValueEntity> streamInstanceFieldValues(long definingInstanceId);

	@Query(nativeQuery = true, value = "select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 0 and FV.definingInstanceId = :definingInstanceId and F.name = :fieldName")
	Optional<FieldValueEntity> findOneByInstanceAndFieldName(long definingInstanceId, String fieldName);

	@Query(nativeQuery = true, value = "select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId")
	Stream<FieldValueEntity> streamStaticFieldValues(long declaringClassId);

	@Query(nativeQuery = true, value = "select FV.* from FieldValue FV inner  join Field F on F.id = FV.fieldId where F.staticFlag = 1 and FV.javaClassId = :declaringClassId and F.declaringClassId = :declaringClassId and F.name = :fieldName")
	Optional<FieldValueEntity> findStaticByClassAndFieldName(long declaringClassId, String fieldName);
}
