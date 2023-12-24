package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.TypeEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Type;

@RequiredArgsConstructor
public class TypeProxy implements Type {
	private final TypeEntity entity;

	@Override
	public String getName() {
		return entity.getName();
	}
}
