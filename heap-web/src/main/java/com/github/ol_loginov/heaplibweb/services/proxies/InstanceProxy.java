package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.Value;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InstanceProxy implements Instance {
	private final InstanceEntity entity;
	private final HeapRepositories heapRepositories;

	public static Instance wrap(InstanceEntity entity, HeapRepositories heapRepositories) {
		return new InstanceProxy(entity, heapRepositories);
	}

	@Override
	public List<FieldValue> getFieldValues() {
		return heapRepositories.getFieldValues().streamByDefiningInstanceId(entity.getInstanceId())
			.map(e -> FieldValueProxy.wrap(entity.getHeapId(), e, heapRepositories))
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
		return HeapRepositories.shouldBeReady(entity.getInstanceNumber());
	}

	@Override
	public JavaClass getJavaClass() {
		return heapRepositories.getJavaClasses().findById(new JavaClassEntity.PK(entity.getHeapId(), entity.getJavaClassId()))
			.map(e -> (JavaClass) new JavaClassProxy(e, heapRepositories))
			.orElse(null);
	}

	@Override
	public long getSize() {
		return entity.getSize();
	}

	@Override
	public long getReachableSize() {
		return HeapRepositories.shouldBeReady(entity.getReachableSize());
	}

	@Override
	public long getRetainedSize() {
		return HeapRepositories.shouldBeReady(entity.getRetainedSize());
	}

	@Override
	public Object getValueOfField(String name) {
		var fieldValueEntity = heapRepositories.getFieldValues()
			.findOneByInstanceAndFieldName(entity.getInstanceId(), name)
			.orElse(null);
		if (fieldValueEntity == null) {
			return null;
		}
		var fieldEntity = heapRepositories.getFields().findById(fieldValueEntity.getFieldId()).orElseThrow();
		var typeEntity = heapRepositories.getTypes().findById(fieldEntity.getTypeId()).orElseThrow();

		return switch (typeEntity.getName()) {
			case "object" -> Optional
				.ofNullable(fieldValueEntity.getValueInstanceId())
				.flatMap(instanceId -> heapRepositories.getInstances().findById(new InstanceEntity.PK(entity.getHeapId(), fieldValueEntity.getValueInstanceId())))
				.map(instanceEntity -> wrap(instanceEntity, heapRepositories))
				.orElseThrow();
			case "byte" -> Byte.parseByte(fieldValueEntity.getValue());
			case "int" -> Integer.parseInt(fieldValueEntity.getValue());
			case "long" -> Long.parseLong(fieldValueEntity.getValue());
			default -> throw new IllegalStateException(typeEntity.getName() + " is not supported");
		};
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
}
