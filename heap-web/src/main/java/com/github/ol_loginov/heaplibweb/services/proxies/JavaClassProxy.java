package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.HeapScope;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class JavaClassProxy implements JavaClass {
	private final JavaClassEntity entity;
	private final HeapScope scope;

	public static JavaClass wrap(JavaClassEntity other, HeapScope scope) {
		return new JavaClassProxy(other, scope);
	}

	private JavaClass wrap(JavaClassEntity other) {
		return wrap(other, scope);
	}

	@Override
	public long getAllInstancesSize() {
		return entity.getAllInstancesSize();
	}

	@Override
	public boolean isArray() {
		return entity.isArray();
	}

	@Override
	public String getName() {
		return entity.getName();
	}

	@Override
	public int getInstanceSize() {
		return entity.getInstanceSize();
	}

	@Override
	public int getInstancesCount() {
		return entity.getInstancesCount();
	}

	@Override
	public long getRetainedSizeByClass() {
		return HeapScope.shouldBeReady(entity.getRetainedSizeByClass());
	}

	@Override
	public long getJavaClassId() {
		return entity.getJavaClassId();
	}

	@Override
	public JavaClass getSuperClass() {
		if (entity.getSuperClassId() == null) {
			return null;
		}
		return scope.getJavaClasses().findById(new JavaClassEntity.PK(entity.getHeapId(), entity.getSuperClassId()))
			.map(this::wrap)
			.orElse(null);
	}

	@Override
	public Collection<JavaClass> getSubClasses() {
		return scope.getJavaClasses().streamAllByHeapIdAndSuperClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(this::wrap)
			.toList();
	}

	@Override
	public List<Field> getFields() {
		return scope.getFields().streamAllByHeapIdAndDeclaringClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(e -> (Field) new FieldProxy(e, scope))
			.toList();
	}

	@Override
	public List<Instance> getInstances() {
		return scope.getInstances().streamAllByHeapIdAndJavaClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(e -> InstanceProxy.wrap(e, scope))
			.toList();
	}

	@Override
	public List<FieldValue> getStaticFieldValues() {
		return scope.getFieldValues().streamStaticFieldValues(entity.getJavaClassId())
			.map(e -> FieldValueProxy.wrap(entity.getHeapId(), e, scope))
			.toList();
	}

	@Override
	public Object getValueOfStaticField(String name) {
		return scope.getFieldValues().findStaticByClassAndFieldName(entity.getJavaClassId(), name)
			.map(e -> FieldValueProxy.getValueObject(e, entity.getHeapId(), scope))
			.orElse(null);
	}


	@Override
	public Instance getClassLoader() {
		return (Instance) getValueOfStaticField("<classLoader>");
	}

	@Override
	public Iterator<Instance> getInstancesIterator() {
		throw new UnsupportedOperationException();
	}
}
