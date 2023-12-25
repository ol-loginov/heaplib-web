package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.heap.FieldValueEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.InstanceEntity;
import com.mysql.cj.MysqlType;
import lombok.RequiredArgsConstructor;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

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

		transactionOperations.executeWithoutResult(st -> {
			var instanceBatchInsert = new InstanceBatchInsert();
			allClasses.forEach(clazz -> {
				persistInstances(heapEntity, clazz, instanceBatchInsert);
				passed.incrementAndGet();
				callback.saveProgress(this);
			});
			instanceBatchInsert.batchInsert(true);
		});

		passed.set(0);
		callback.saveProgress(this, true);

		allClasses.forEach(clazz -> transactionOperations.executeWithoutResult(st -> {
			persistFieldValues(clazz, classFieldLookup);
			passed.incrementAndGet();
			callback.saveProgress(this);
		}));
	}

	class InstanceBatchInsert implements Consumer<InstanceEntity> {
		List<InstanceEntity> instanceList = new ArrayList<InstanceEntity>();

		@Override
		public void accept(InstanceEntity instanceEntity) {
			instanceList.add(instanceEntity);
			batchInsert(false);
		}

		private void batchInsert(boolean force) {
			if (!force && instanceList.size() < 1000) {
				return;
			}

			heapRepositories.getJdbc().batchUpdate("insert into Instance(heapId,instanceId,instanceNumber,javaClassId,gcRoot,size,retainedSize,reachableSize) values(?,?,?,?,?,?,?,?)", instanceList, 100, (ps, instance) -> {
				ps.setInt(1, instance.getHeapId());
				ps.setLong(2, instance.getInstanceId());
				ps.setInt(3, instance.getInstanceNumber());
				ps.setLong(4, instance.getJavaClassId());
				ps.setBoolean(5, instance.isGcRoot());
				ps.setLong(6, instance.getSize());
				ps.setLong(7, instance.getRetainedSize());
				ps.setLong(8, instance.getReachableSize());
			});
			instanceList = new ArrayList<>();
		}
	}

	private void persistInstances(HeapEntity heapEntity, JavaClass clazz, Consumer<InstanceEntity> instanceEntityConsumer) {
		var instanceNumber = new AtomicInteger();
		for (var instance : clazz.getInstances()) {
			instancesLoaded.incrementAndGet();
			instanceEntityConsumer.accept(new InstanceEntity(heapEntity.getId(), instance.getInstanceId(), instanceNumber.incrementAndGet(), clazz.getJavaClassId(),
				instance.isGCRoot(), instance.getSize(), NUMBER_NOT_READY, NUMBER_NOT_READY));
		}
	}

	private void persistFieldValues(JavaClass clazz, ClassFieldLookup classFieldLookup) {
		var fieldValues = new ArrayList<FieldValueEntity>();

		for (var fieldValue : clazz.getStaticFieldValues()) {
			fieldsLoaded.incrementAndGet();
			var fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.getField());
			var fieldValueInstance = new FieldValueEntity(clazz.getJavaClassId(), fieldValue.getDefiningInstance().getInstanceId(), fieldEntityId, true, fieldValue.getValue(), null);
			if (fieldValue instanceof ObjectFieldValue objectFieldValue) {
				fieldValueInstance.setValueInstanceId(objectFieldValue.getInstanceId());
			}
			fieldValues.add(fieldValueInstance);
		}

		for (var instance : clazz.getInstances()) {
			for (var fieldValue : instance.getFieldValues()) {
				fieldsLoaded.incrementAndGet();
				var fieldEntityId = classFieldLookup.getFieldEntityId(fieldValue.getField());
				assert fieldValue.getDefiningInstance().getInstanceId() == instance.getInstanceId();

				var fieldValueInstance = new FieldValueEntity(clazz.getJavaClassId(), fieldValue.getDefiningInstance().getInstanceId(), fieldEntityId, false, fieldValue.getValue(), null);
				if (fieldValue instanceof ObjectFieldValue objectFieldValue) {
					fieldValueInstance.setValueInstanceId(objectFieldValue.getInstanceId());
				}
				fieldValues.add(fieldValueInstance);
			}
		}

		heapRepositories.getJdbc().batchUpdate("insert into FieldValue(javaClassId,definingInstanceId,fieldId,staticFlag,value,valueInstanceId) values(?,?,?,?,?,?)", fieldValues, 100, (ps, instance) -> {
			ps.setLong(1, instance.getJavaClassId());
			ps.setLong(2, instance.getDefiningInstanceId());
			ps.setInt(3, instance.getFieldId());
			ps.setBoolean(4, instance.isStaticFlag());
			ps.setString(5, instance.getValue());

			if (instance.getValueInstanceId() == null) {
				ps.setNull(6, MysqlType.BIGINT.getJdbcType());
			} else {
				ps.setLong(6, instance.getValueInstanceId());
			}
		});
	}
}
