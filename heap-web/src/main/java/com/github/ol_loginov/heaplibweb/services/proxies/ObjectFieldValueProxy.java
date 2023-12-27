package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;

public class ObjectFieldValueProxy extends FieldValueProxy implements ObjectFieldValue {
	public ObjectFieldValueProxy(int heapId, FieldValueEntity entity, HeapScope scope) {
		super(heapId, entity, scope);
	}

	@Override
	public Instance getInstance() {
		return scope.getInstances().findById(new InstanceEntity.PK(heapId, entity.getDefiningInstanceId()))
			.map(e -> new InstanceProxy(e, scope))
			.orElse(null);
	}

	@Override
	public long getInstanceId() {
		return entity.getValueInstanceId();
	}
}
