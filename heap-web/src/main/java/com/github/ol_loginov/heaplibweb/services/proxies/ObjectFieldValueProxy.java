package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;

public class ObjectFieldValueProxy extends FieldValueProxy implements ObjectFieldValue {
	public ObjectFieldValueProxy(int heapId, FieldValueEntity entity, HeapRepositories heapRepositories) {
		super(heapId, entity, heapRepositories);
	}

	@Override
	public Instance getInstance() {
		return heapRepositories.getInstances().findById(new InstanceEntity.PK(heapId, entity.getDefiningInstanceId()))
			.map(e -> new InstanceProxy(e, heapRepositories))
			.orElse(null);
	}

	@Override
	public long getInstanceId() {
		return entity.getValueInstanceId();
	}
}
