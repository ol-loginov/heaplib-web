package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.Value;

import java.util.List;

@RequiredArgsConstructor
public class InstanceProxy implements Instance {
	private final InstanceEntity entity;
	private final HeapScope scope;

	public static Instance wrap(InstanceEntity entity, HeapScope scope) {
		return new InstanceProxy(entity, scope);
	}

	@Override
	public List<FieldValue> getFieldValues() {
		return scope.getFieldValues().streamInstanceFieldValues(entity.getInstanceId())
			.map(e -> FieldValueProxy.wrap(entity.getHeapId(), e, scope))
			.toList();
	}

	@Override
	public boolean isGCRoot() {
		return entity.isGcRoot();
	}

	@Override
	public long getInstanceId() {
		return entity.getInstanceId();
	}

	@Override
	public int getInstanceNumber() {
		return HeapScope.shouldBeReady(entity.getInstanceNumber());
	}

	@Override
	public JavaClass getJavaClass() {
		return scope.getJavaClasses().findById(new JavaClassEntity.PK(entity.getHeapId(), entity.getJavaClassId()))
			.map(e -> (JavaClass) new JavaClassProxy(e, scope))
			.orElse(null);
	}

	@Override
	public long getSize() {
		return entity.getSize();
	}

	@Override
	public long getReachableSize() {
		return HeapScope.shouldBeReady(entity.getReachableSize());
	}

	@Override
	public long getRetainedSize() {
		return HeapScope.shouldBeReady(entity.getRetainedSize());
	}

	@Override
	public Object getValueOfField(String name) {
		var fieldValueEntity = scope.getFieldValues()
			.findOneByInstanceAndFieldName(entity.getInstanceId(), name)
			.orElse(null);
		if (fieldValueEntity == null) {
			return null;
		}
		return FieldValueProxy.getValueObject(fieldValueEntity, entity.getHeapId(), scope);
	}

	@Override
	public Instance getNearestGCRootPointer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Value> getReferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FieldValue> getStaticFieldValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + entity.getInstanceId();
	}
}
