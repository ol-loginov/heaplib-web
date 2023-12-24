package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity;
import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ClassFieldLookup {
	private final HeapEntity heapEntity;
	private final HeapRepositories heapRepositories;
	private final TypeIdLookup typeIdLookup;
	private Map<FieldKey, Integer> fieldEntities;

	private record FieldKey(long declaringClassId, String name, boolean isStatic) {
	}

	public int getFieldEntityId(Field field) {
		if (fieldEntities == null) {
			fieldEntities = heapRepositories.getFields().streamAllByHeapId(heapEntity.getId())
				.collect(Collectors.toMap(
					e -> new FieldKey(e.getDeclaringClassId(), e.getName(), e.isStaticFlag()),
					EntityIdentity::getId
				));
		}
		var fieldKey = new FieldKey(field.getDeclaringClass().getJavaClassId(), field.getName(), field.isStatic());
		return fieldEntities.computeIfAbsent(fieldKey, k -> this.createField(field));
	}

	private int createField(Field field) {
		var fieldEntity = new FieldEntity(heapEntity.getId(),
			field.getDeclaringClass().getJavaClassId(), field.getName(), field.isStatic(),
			typeIdLookup.lookupTypeId(field.getType().getName()));
		heapRepositories.persist(fieldEntity);
		return fieldEntity.getId();
	}
}