package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.netbeans.lib.profiler.heap.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
public class HeapProxy implements Heap {
	private final HeapEntity entity;
	private final HeapRepositories heapRepositories;

	protected JavaClassRepository getJavaClasses() {
		return heapRepositories.getJavaClasses();
	}

	protected FieldRepository getFields() {
		return heapRepositories.getFields();
	}

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public List<JavaClass> getAllClasses() {
		return getJavaClasses().streamAllByHeapId(entity.getId())
			.map(this::proxyJavaClass)
			.toList();
	}

	private JavaClass proxyJavaClass(JavaClassEntity e) {
		return new JavaClassProxy(e, heapRepositories);
	}

	@Override
	public JavaClass getJavaClassByID(long javaclassId) {
		return getJavaClasses().findById(new JavaClassEntity.PK(entity.getId(), javaclassId))
			.map(this::proxyJavaClass)
			.orElse(null);
	}

	@Override
	public JavaClass getJavaClassByName(String fqn) {
		return getJavaClasses().findByHeapIdAndName(entity.getId(), fqn)
			.map(this::proxyJavaClass)
			.orElse(null);
	}

	@Override
	public Collection<JavaClass> getJavaClassesByRegExp(String regexp) {
		return getJavaClasses().findAllByHeapIdAndNameMatches(entity.getId(), regexp)
			.map(this::proxyJavaClass)
			.toList();
	}

	@Override
	public Iterable<Instance> getAllInstances() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Instance> getAllInstances(long instanceID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Instance> getBiggestObjectsByRetainedSize(int number) {
		throw new UnsupportedOperationException();
	}

	@Override
	public GCRoot getGCRoot(Instance instance) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<GCRoot> getGCRoots() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instance getInstanceByID(long instanceId) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Iterator<Instance> getAllInstancesIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HeapSummary getSummary() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Properties getSystemProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRetainedSizeComputed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRetainedSizeByClassComputed() {
		throw new UnsupportedOperationException();
	}
}
