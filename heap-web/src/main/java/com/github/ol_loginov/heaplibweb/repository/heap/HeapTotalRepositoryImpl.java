package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.SequenceIdentity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Inject))
class HeapTotalRepositoryImpl implements HeapTotalRepository {
	private final EntityManager entityManager;

	@Override
	public void persist(SequenceIdentity entity) {
		entityManager.persist(entity);
	}
}
