package com.github.ol_loginov.heaplibweb.services.loaders;

import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepository;
import com.github.ol_loginov.heaplibweb.services.InputFilesManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.lib.profiler.heap.HeapFactory2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * @see org.netbeans.lib.profiler.heap.Value
 * @see org.netbeans.lib.profiler.heap.FieldValue
 * @see org.netbeans.lib.profiler.heap.ObjectFieldValue
 * @see org.netbeans.lib.profiler.heap.ArrayItemValue
 * <p/>
 * @see org.netbeans.lib.profiler.heap.GCRoot
 * @see org.netbeans.lib.profiler.heap.ThreadObjectGCRoot
 * @see org.netbeans.lib.profiler.heap.JavaFrameGCRoot
 * <p/>
 * @see org.netbeans.lib.profiler.heap.Instance
 * @see org.netbeans.lib.profiler.heap.ObjectArrayInstance
 * @see org.netbeans.lib.profiler.heap.PrimitiveArrayInstance
 * <p/>
 * @see org.netbeans.lib.profiler.heap.Type
 * @see org.netbeans.lib.profiler.heap.JavaClass
 * @see org.netbeans.lib.profiler.heap.PrimitiveType
 * <p/>
 * @see org.netbeans.lib.profiler.heap.Field
 * @see org.netbeans.lib.profiler.heap.Heap
 * @see org.netbeans.lib.profiler.heap.HeapSummary
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputLoader implements Runnable, Task.Callback {
	private static final long DEFAULT_BUFFER_MB = 100L;
	private final TransactionOperations transactionOperations;
	private final HeapFileRepository heapFileRepository;
	private final HeapRepository heapRepository;
	private final HeapRepositories heapRepositories;
	private final InputFilesManager inputLoadWorkFactory;

	private int entityId;

	private long progressLimit = 1;
	private long progressCurrent = 0;
	private volatile Instant progressSaved = Instant.now();

	public InputLoader withEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	private HeapFile loadFileEntity() {
		return transactionOperations.execute(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			entity.setStatus(HeapFileStatus.LOADING);
			heapFileRepository.save(entity);
			return entity;
		});
	}

	public void saveProgress(Task task, boolean force) {
		if (!force && Duration.between(progressSaved, Instant.now()).toMillis() < 1000) {
			return;
		}

		var progress = progressLimit > 0 ? Math.round(1000 * (progressCurrent / (double) progressLimit)) : 0;
		transactionOperations.executeWithoutResult(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			entity.setLoadProgress(progress / 1000f);
			entity.setLoadMessage(task.getText());
			heapFileRepository.save(entity);
		});
		log.info("progress '{}': {}", task.getText(), progress / 10d);
		progressSaved = Instant.now();
	}

	@Override
	public void run() {
		try {
			runUnsafe();
		} catch (Throwable e) {
			log.error("load failure: {}", e.getMessage(), e);
		}
	}


	private void runUnsafe() throws IOException {
		var entity = loadFileEntity();
		var dump = inputLoadWorkFactory.getInputFile(entity.getRelativePath()).toFile();

		var heap = HeapFactory2.canBeMemMapped(dump)
			? HeapFactory2.createFastHeap(dump)
			: HeapFactory2.createFastHeap(dump, DEFAULT_BUFFER_MB * 1024 * 1024);

		var heapEntity = new HeapEntity();
		heapEntity.setFile(entity);
		heapRepository.save(heapEntity);

		var summary = heap.getSummary();
		var allClasses = heap.getAllClasses();

		progressLimit = allClasses.size() * 3L + summary.getTotalAllocatedInstances();
		progressCurrent = 0;

		var typeIdLookup = new TypeIdLookup(heapEntity.getId(), heapRepositories);

		runStep(new LoadJavaClasses(heap, heapEntity, transactionOperations, heapRepositories));
		runStep(new LoadJavaClassFields(heap, heapEntity, transactionOperations, heapRepositories, typeIdLookup));
		runStep(new LoadInstances(heap, heapEntity, transactionOperations, heapRepositories, typeIdLookup));
	}

	private void runStep(Task task) {
		task.run(this);
	}
}
