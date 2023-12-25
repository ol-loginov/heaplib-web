package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;

import java.util.Optional;

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

	public static Object getValueObject(FieldValueEntity fieldValue, int heapId, HeapRepositories heapRepositories) {
		var fieldEntity = heapRepositories.getFields().findById(fieldValue.getFieldId()).orElseThrow();
		var typeEntity = heapRepositories.getTypes().findById(fieldEntity.getTypeId()).orElseThrow();

		return switch (typeEntity.getName()) {
			case "object" -> Optional
				.ofNullable(fieldValue.getValueInstanceId())
				.flatMap(instanceId -> heapRepositories.getInstances().findById(new InstanceEntity.PK(heapId, fieldValue.getValueInstanceId())))
				.map(instanceEntity -> InstanceProxy.wrap(instanceEntity, heapRepositories))
				.orElse(null);
			case "boolean" -> !"0".equals(fieldValue.getValue());
			case "byte" -> Byte.parseByte(fieldValue.getValue());
			case "short" -> Short.parseShort(fieldValue.getValue());
			case "int" -> Integer.parseInt(fieldValue.getValue());
			case "long" -> Long.parseLong(fieldValue.getValue());
			case "char" -> fieldValue.getValue().charAt(0);
			case "float" -> Float.parseFloat(fieldValue.getValue());
			case "double" -> Double.parseDouble(fieldValue.getValue());
			default -> throw new IllegalStateException(typeEntity.getName() + " is not supported");
		};
	}

	@Override
	public Field getField() {
		return new FieldProxy(heapRepositories.getFields().findById(entity.getFieldId()).orElseThrow(), heapRepositories);
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
