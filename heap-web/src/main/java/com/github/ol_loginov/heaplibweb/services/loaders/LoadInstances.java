package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.springframework.transaction.support.TransactionOperations;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories.NUMBER_NOT_READY;

@RequiredArgsConstructor
class LoadInstances implements Task {
	private final Heap heap;
	private final HeapEntity heapEntity;

	private final TransactionOperations transactionOperations;
	private final HeapRepositories heapRepositories;
	private final TypeIdLookup typeIdLookup;

	private final AtomicLong passed = new AtomicLong();
	private volatile long total;

	private final AtomicLong instancesLoaded = new AtomicLong();
	private final AtomicLong fieldsLoaded = new AtomicLong();

	@Override
	public String getText() {
		return "persist instances: " + passed + "/" + total + " (instances=" + instancesLoaded.get() + ", fields=" + fieldsLoaded.get() + ")";
	}

	@Override
	public void run(Callback callback) {
		var classFieldLookup = new ClassFieldLookup(heapEntity, heapRepositories, typeIdLookup);

		var allClasses = heap.getAllClasses();
		passed.set(0);
		total = allClasses.size();
		callback.saveProgress(this, true);

		allClasses.forEach(clazz -> transactionOperations.executeWithoutResult(st -> {
			persistJavaClassInstances(heapEntity, clazz, classFieldLookup);
			passed.incrementAndGet();
			callback.saveProgress(this);
		}));
	}

	private void persistJavaClassInstances(HeapEntity heapEntity, JavaClass clazz, ClassFieldLookup classFieldLookup) {
		for (var instance : clazz.getInstances()) {
			instancesLoaded.incrementAndGet();

			var instanceEntity = new InstanceEntity(heapEntity.getId(), instance.getInstanceId(), NUMBER_NOT_READY, clazz.getJavaClassId(),
				instance.isGCRoot(), instance.getSize(), NUMBER_NOT_READY, NUMBER_NOT_READY);
			heapRepositories.persist(instanceEntity);

			for (var fieldValue : instance.getFieldValues()) {
				fieldsLoaded.incrementAndGet();
				var fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.getField());
				assert fieldValue.getDefiningInstance().getInstanceId() == instance.getInstanceId();

				var fieldValueInstance = new FieldValueEntity(clazz.getJavaClassId(), fieldValue.getDefiningInstance().getInstanceId(), fieldEntityId, false, fieldValue.getValue(), null);
				if (fieldValue instanceof ObjectFieldValue objectFieldValue) {
					fieldValueInstance.setValueInstanceId(objectFieldValue.getInstanceId());
				}
				heapRepositories.persist(fieldValueInstance);
			}
		}

		for (var fieldValue : clazz.getStaticFieldValues()) {
			fieldsLoaded.incrementAndGet();
			var fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.getField());
			var fieldValueInstance = new FieldValueEntity(clazz.getJavaClassId(), fieldValue.getDefiningInstance().getInstanceId(), fieldEntityId, true, fieldValue.getValue(), null);
			if (fieldValue instanceof ObjectFieldValue objectFieldValue) {
				fieldValueInstance.setValueInstanceId(objectFieldValue.getInstanceId());
			}
			heapRepositories.persist(fieldValueInstance);
		}

		heapRepositories.flush();
	}
}
