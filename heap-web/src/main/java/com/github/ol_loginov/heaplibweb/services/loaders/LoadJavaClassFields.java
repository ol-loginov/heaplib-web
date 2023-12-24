package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.transaction.support.TransactionOperations;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
class LoadJavaClassFields implements Task {
	private final Heap heap;
	private final HeapEntity heapEntity;

	private final TransactionOperations transactionOperations;
	private final HeapRepositories heapRepositories;
	private final TypeIdLookup typeIdLookup;

	private final AtomicLong passed = new AtomicLong();
	private volatile long total;
	private final AtomicLong fieldsLoaded = new AtomicLong();

	@Override
	public String getText() {
		return "persist fields: " + passed + "/" + total + " (fields=" + fieldsLoaded.get() + ")";
	}

	@Override
	public void run(Callback callback) {
		var all = heap.getAllClasses();
		total = all.size();
		passed.set(0);
		callback.saveProgress(this, true);

		all.forEach(clazz -> transactionOperations.executeWithoutResult(st -> {
			persistJavaClassFields(heapEntity, clazz, typeIdLookup);
			passed.incrementAndGet();
			callback.saveProgress(this);
		}));
	}

	private void persistJavaClassFields(HeapEntity heapEntity, JavaClass clazz, TypeIdLookup nameLookup) {
		for (var field : clazz.getFields()) {
			fieldsLoaded.incrementAndGet();

			var fieldEntity = new FieldEntity(heapEntity.getId(), clazz.getJavaClassId(),
				field.getName(), field.isStatic(),
				nameLookup.lookupTypeId(field.getType().getName()));
			heapRepositories.persist(fieldEntity);
		}
		heapRepositories.flush();
	}
}
