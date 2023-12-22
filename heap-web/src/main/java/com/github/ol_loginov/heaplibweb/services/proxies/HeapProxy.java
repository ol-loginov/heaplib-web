package com.github.ol_loginov.heaplibweb.services.proxies;

import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntityRepository;
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
	private final JavaClassEntityRepository javaClassEntityRepository;

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public List<JavaClass> getAllClasses() {
		return javaClassEntityRepository.streamAllByHeap(entity)
			.map(e -> (JavaClass) new JavaClassProxy(e))
			.toList();
	}

	@Override
	public Iterable<Instance> getAllInstances() {
		throw new NotImplementedException();
	}

	@Override
	public Iterable<Instance> getAllInstances(long instanceID) {
		throw new NotImplementedException();
	}

	@Override
	public List<Instance> getBiggestObjectsByRetainedSize(int number) {
		throw new NotImplementedException();
	}

	@Override
	public GCRoot getGCRoot(Instance instance) {
		throw new NotImplementedException();
	}

	@Override
	public Collection<GCRoot> getGCRoots() {
		throw new NotImplementedException();
	}

	@Override
	public Instance getInstanceByID(long instanceId) {
		throw new NotImplementedException();
	}

	@Override
	public JavaClass getJavaClassByID(long javaclassId) {
		throw new NotImplementedException();
	}

	@Override
	public JavaClass getJavaClassByName(String fqn) {
		throw new NotImplementedException();
	}

	@Override
	public Collection<JavaClass> getJavaClassesByRegExp(String regexp) {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<Instance> getAllInstancesIterator() {
		throw new NotImplementedException();
	}

	@Override
	public HeapSummary getSummary() {
		throw new NotImplementedException();
	}

	@Override
	public Properties getSystemProperties() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isRetainedSizeComputed() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isRetainedSizeByClassComputed() {
		throw new NotImplementedException();
	}
}
