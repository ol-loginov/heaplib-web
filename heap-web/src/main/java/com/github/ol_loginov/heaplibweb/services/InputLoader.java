package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntityRepository;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapTotalRepository;
import com.github.ol_loginov.heaplibweb.repository.heap.JavaClassEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory2;
import org.netbeans.lib.profiler.heap.HeapOperationUnsupportedException;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.io.IOException;
import java.util.function.LongSupplier;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputLoader implements Runnable {
	private static final long DEFAULT_BUFFER_MB = 100L;
	private final TransactionOperations transactionOperations;
	private final HeapFileRepository heapFileRepository;
	private final HeapEntityRepository heapEntityRepository;
	private final HeapTotalRepository heapTotalRepository;
	private final InputFilesManager inputLoadWorkFactory;

	private int entityId;

	private long progressLimit = 1;
	private long progressCurrent = 0;

	public InputLoader withEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	private void incrementProgress() {
		var oldValue = Math.round(1000f * (progressCurrent / (double) progressLimit));
		progressCurrent += 1;
		var newValue = Math.round(1000f * (progressCurrent / (double) progressLimit));
		if (newValue != oldValue) {
			saveProgress();
			log.info("progress {}", newValue);
		}
	}

	private HeapFile loadFileEntity() {
		var result = transactionOperations.execute(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			entity.setStatus(HeapFileStatus.LOADING);
			heapFileRepository.save(entity);

			return entity;
		});

		saveProgress();

		return result;
	}

	private void saveProgress() {
		var progress = Math.round(1000 * (progressCurrent / (double) progressLimit));
		transactionOperations.executeWithoutResult(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			entity.setLoadProgress(progress / 1000f);
			heapFileRepository.save(entity);
		});
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

		Heap heap;
		if (!HeapFactory2.canBeMemMapped(dump)) {
			heap = HeapFactory2.createFastHeap(dump, DEFAULT_BUFFER_MB * 1024 * 1024);
		} else {
			heap = HeapFactory2.createFastHeap(dump);
		}

		var heapEntity = new HeapEntity();
		heapEntity.setFile(entity);
		heapEntityRepository.save(heapEntity);

		var summary = heap.getSummary();
		var allClasses = heap.getAllClasses();

		progressLimit = allClasses.size() + summary.getTotalAllocatedInstances();
		progressCurrent = 0;
		saveProgress();

		heap.getAllClasses().forEach(clazz -> {
			persistEntity(heapEntity, clazz);
			incrementProgress();
		});
	}

	private void persistEntity(HeapEntity heapEntity, JavaClass clazz) {
		var clazzEntity = new JavaClassEntity(heapEntity,
			clazz.getName(),
			clazz.getAllInstancesSize(),
			clazz.isArray(),
			clazz.getInstanceSize(),
			clazz.getInstancesCount(),
			minusOneOnError(clazz::getRetainedSizeByClass),
			clazz.getJavaClassId());
		heapTotalRepository.persist(clazzEntity);
	}

	private long minusOneOnError(LongSupplier supplier) {
		try {
			return supplier.getAsLong();
		} catch (HeapOperationUnsupportedException e) {
			return -1;
		}
	}
}
