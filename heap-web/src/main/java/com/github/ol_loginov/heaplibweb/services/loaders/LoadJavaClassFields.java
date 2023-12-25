package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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
		return "import class fields: " + passed + "/" + total + " (fields=" + fieldsLoaded.get() + ")";
	}

	@Override
	public void run(Callback callback) {
		var all = heap.getAllClasses();
		total = all.size();
		passed.set(0);
		callback.saveProgress(this, true);

		var persister = new Persister();

		all.forEach(clazz -> transactionOperations.executeWithoutResult(st -> {
			persistJavaClassFields(heapEntity, clazz, typeIdLookup, persister);
			passed.incrementAndGet();
			callback.saveProgress(this);
		}));

		persister.batchInsert(true);
	}

	private void persistJavaClassFields(HeapEntity heapEntity, JavaClass clazz, TypeIdLookup nameLookup, Consumer<FieldEntity> fieldEntityConsumer) {
		for (var field : clazz.getFields()) {
			fieldsLoaded.incrementAndGet();
			fieldEntityConsumer.accept(new FieldEntity(heapEntity.getId(), clazz.getJavaClassId(),
				field.getName(), field.isStatic(),
				nameLookup.lookupTypeId(field.getType().getName())));
		}

		for (var fieldValue : clazz.getStaticFieldValues()) {
			fieldsLoaded.incrementAndGet();
			var field = fieldValue.getField();
			fieldEntityConsumer.accept(new FieldEntity(heapEntity.getId(), field.getDeclaringClass().getJavaClassId(),
				field.getName(), field.isStatic(),
				nameLookup.lookupTypeId(field.getType().getName())));
		}
	}

	@RequiredArgsConstructor
	class Persister implements Consumer<FieldEntity> {
		private ArrayList<FieldEntity> batch = new ArrayList<>();

		@Override
		public void accept(FieldEntity fieldEntity) {
			batch.add(fieldEntity);
			batchInsert(false);
		}

		private void batchInsert(boolean force) {
			if (!force && batch.size() < 1000) {
				return;
			}

			heapRepositories.getJdbc().batchUpdate("insert into Field(heapId,declaringClassId,name,staticFlag,typeId) values(?,?,?,?,?)", batch, 100, (ps, entity) -> {
				ps.setInt(1, entity.getHeapId());
				ps.setLong(2, entity.getDeclaringClassId());
				ps.setString(3, entity.getName());
				ps.setBoolean(4, entity.isStaticFlag());
				ps.setInt(5, entity.getTypeId());
			});
			batch = new ArrayList<>();
		}
	}

}
