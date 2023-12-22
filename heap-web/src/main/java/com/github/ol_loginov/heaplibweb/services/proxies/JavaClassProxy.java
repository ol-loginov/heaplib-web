package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
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
	private final JavaClassEntityRepository entityRepository;

	private JavaClass swap(JavaClassEntity other) {
		return new JavaClassProxy(other, entityRepository);
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
		return entity.getRetainedSizeByClass();
	}

	@Override
	public long getJavaClassId() {
		return entity.getJavaClassId();
	}

	@Override
	public JavaClass getSuperClass() {
		return Optional.ofNullable(entity.getSuperClass())
			.map(this::swap)
			.orElse(null);
	}

	@Override
	public Collection<JavaClass> getSubClasses() {
		return entityRepository.streamAllBySuperClass(entity)
			.map(this::swap)
			.toList();
	}

	@Override
	public Instance getClassLoader() {
		throw new NotImplementedException();
	}

	@Override
	public List<Field> getFields() {
		throw new NotImplementedException();
	}

	@Override
	public List<Instance> getInstances() {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<Instance> getInstancesIterator() {
		throw new NotImplementedException();
	}

	@Override
	public List<FieldValue> getStaticFieldValues() {
		throw new NotImplementedException();
	}

	@Override
	public Object getValueOfStaticField(String name) {
		throw new NotImplementedException();
	}
}
