package com.github.ol_loginov.heaplibweb.services.loaders;


import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
class LoadJavaClasses implements Task {
	private final Heap heap;
	private final HeapEntity heapEntity;
	private final TransactionOperations transactionOperations;
	private final HeapRepositories heapRepositories;

	private final AtomicLong passed = new AtomicLong();
	private volatile long total;

	@Override
	public String getText() {
		return "persist java classes: " + passed + "/" + total;
	}

	@Override
	public void run(Callback callback) {
		var all = heap.getAllClasses();
		total = all.size();
		passed.set(0);

		callback.saveProgress(this, true);
		heap.getAllClasses().forEach(clazz -> transactionOperations.executeWithoutResult(st -> {
			persistJavaClass(heapEntity, clazz);
			passed.incrementAndGet();
			callback.saveProgress(this);
		}));
	}

	private void persistJavaClass(HeapEntity heapEntity, JavaClass clazz) {
		var clazzEntity = new JavaClassEntity(
			heapEntity.getId(), clazz.getJavaClassId(),
			clazz.getName(),
			clazz.getAllInstancesSize(),
			clazz.isArray(),
			clazz.getInstanceSize(),
			clazz.getInstancesCount(),
			Task.notReadyValueOnError(clazz::getRetainedSizeByClass),
			Optional.ofNullable(clazz.getSuperClass()).map(JavaClass::getJavaClassId).orElse(null));
		heapRepositories.persist(clazzEntity);
	}
}