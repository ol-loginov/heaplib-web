package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JavaClassProxy implements JavaClass {
	private final JavaClassEntity entity;
	private final HeapRepositories heapRepositories;

	public static JavaClass wrap(JavaClassEntity other, HeapRepositories heapRepositories) {
		return new JavaClassProxy(other, heapRepositories);
	}

	private JavaClass wrap(JavaClassEntity other) {
		return wrap(other, heapRepositories);
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
		return HeapRepositories.shouldBeReady(entity.getRetainedSizeByClass());
	}

	@Override
	public long getJavaClassId() {
		return entity.getJavaClassId();
	}

	@Override
	public JavaClass getSuperClass() {
		return Optional.of(new JavaClassEntity.PK(entity.getHeapId(), entity.getSuperClassId()))
			.flatMap(heapRepositories.getJavaClasses()::findById)
			.map(this::wrap)
			.orElse(null);
	}

	@Override
	public Collection<JavaClass> getSubClasses() {
		return heapRepositories.getJavaClasses().streamAllByHeapIdAndSuperClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(this::wrap)
			.toList();
	}

	@Override
	public List<Field> getFields() {
		return heapRepositories.getFields().streamAllByHeapIdAndDeclaringClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(e -> (Field) new FieldProxy(e, heapRepositories))
			.toList();
	}

	@Override
	public List<Instance> getInstances() {
		return heapRepositories.getInstances().streamAllByHeapIdAndJavaClassId(entity.getHeapId(), entity.getJavaClassId())
			.map(e -> InstanceProxy.wrap(e, heapRepositories))
			.toList();
	}

	@Override
	public Instance getClassLoader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Instance> getInstancesIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<FieldValue> getStaticFieldValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getValueOfStaticField(String name) {
		throw new UnsupportedOperationException();
	}
}
