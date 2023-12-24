package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.TypeEntity;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
class TypeIdLookup {
	private final int heapId;
	private final HeapRepositories heapRepositories;
	private final HashMap<String, Integer> cache = new HashMap<>();

	public int lookupTypeId(String name) {
		return cache.computeIfAbsent(name, this::registerType);
	}

	private int registerType(String name) {
		var typeEntity = new TypeEntity(heapId, name);
		heapRepositories.persist(typeEntity);
		return typeEntity.getId();
	}
}
