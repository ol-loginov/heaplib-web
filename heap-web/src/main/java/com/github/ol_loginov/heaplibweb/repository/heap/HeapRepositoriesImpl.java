package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
class HeapRepositoriesImpl implements HeapRepositories {
	@PersistenceContext
	private EntityManager entityManager;
	@Getter
	private final JavaClassRepository javaClasses;
	@Getter
	private final FieldRepository fields;
	@Getter
	private final FieldValueRepository fieldValues;
	@Getter
	private final TypeRepository types;
	@Getter
	private final InstanceRepository instances;

	@Override
	public void persist(EntityInstance entity) {
		entityManager.persist(entity);
	}

	@Override
	public void flush() {
		entityManager.flush();
	}
}
