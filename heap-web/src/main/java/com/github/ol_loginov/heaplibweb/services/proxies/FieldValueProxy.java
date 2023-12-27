package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope;
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
	protected final HeapScope scope;

	public static FieldValue wrap(int heapId, FieldValueEntity entity, HeapScope scope) {
		return entity.getValueInstanceId() == null
			? new FieldValueProxy(heapId, entity, scope)
			: new ObjectFieldValueProxy(heapId, entity, scope);
	}

	public static Object getValueObject(FieldValueEntity fieldValue, int heapId, HeapScope scope) {
		var fieldEntity = scope.getFields().findById(fieldValue.getFieldId()).orElseThrow();
		var typeEntity = scope.getTypes().findById(fieldEntity.getTypeId()).orElseThrow();

		return switch (typeEntity.getName()) {
			case "object" -> Optional
				.ofNullable(fieldValue.getValueInstanceId())
				.flatMap(instanceId -> scope.getInstances().findById(new InstanceEntity.PK(heapId, fieldValue.getValueInstanceId())))
				.map(instanceEntity -> InstanceProxy.wrap(instanceEntity, scope))
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
		return new FieldProxy(scope.getFields().findById(entity.getFieldId()).orElseThrow(), scope);
	}

	@Override
	public String getValue() {
		return entity.getValue();
	}

	@Override
	public Instance getDefiningInstance() {
		return scope.getInstances().findById(new InstanceEntity.PK(heapId, entity.getDefiningInstanceId()))
			.map(e -> new InstanceProxy(e, scope))
			.orElse(null);
	}
}
