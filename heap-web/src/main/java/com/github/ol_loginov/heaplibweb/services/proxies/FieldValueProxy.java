package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;

@RequiredArgsConstructor
public class FieldValueProxy implements FieldValue {
	protected final int heapId;
	protected final FieldValueEntity entity;
	protected final HeapRepositories heapRepositories;

	public static FieldValue wrap(int heapId, FieldValueEntity entity, HeapRepositories heapRepositories) {
		return entity.getValueInstanceId() == null
			? new FieldValueProxy(heapId, entity, heapRepositories)
			: new ObjectFieldValueProxy(heapId, entity, heapRepositories);
	}

	@Override
	public Field getField() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getValue() {
		return entity.getValue();
	}

	@Override
	public Instance getDefiningInstance() {
		return heapRepositories.getInstances().findById(new InstanceEntity.PK(heapId, entity.getDefiningInstanceId()))
			.map(e -> new InstanceProxy(e, heapRepositories))
			.orElse(null);
	}
}
