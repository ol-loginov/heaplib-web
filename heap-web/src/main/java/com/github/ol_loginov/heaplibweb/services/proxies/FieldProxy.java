package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.Type;

@RequiredArgsConstructor
public class FieldProxy implements Field {
	private final FieldEntity entity;
	private final HeapRepositories heapRepositories;

	@Override
	public JavaClass getDeclaringClass() {
		return null;
	}

	@Override
	public String getName() {
		return entity.getName();
	}

	@Override
	public boolean isStatic() {
		return entity.isStaticFlag();
	}

	@Override
	public Type getType() {
		return heapRepositories.getTypes().findById(entity.getTypeId())
			.map(e -> (Type) new TypeProxy(e))
			.orElse(null);
	}
}
