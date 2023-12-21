package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import com.github.ol_loginov.heaplibweb.repository.HeapFileStatus;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntity;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory2;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputLoader implements Runnable {
	private final TransactionOperations transactionOperations;
	private final HeapFileRepository heapFileRepository;
	private final HeapEntityRepository heapEntityRepository;
	private final InputFilesManager inputLoadWorkFactory;

	private int entityId;

	private long bufferSizeMb = 100L;

	private long progressLimit = 1;
	private long progressCurrent = 0;

	public InputLoader withEntityId(int entityId) {
		this.entityId = entityId;
		return this;
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
			heap = HeapFactory2.createFastHeap(dump, bufferSizeMb << 20);
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
		updateProgress();

		heap.getAllClasses().forEach(clazz -> {
			progressLimit += 1;
		});
	}

	private HeapFile loadFileEntity() {
		var result = transactionOperations.execute(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			entity.setStatus(HeapFileStatus.LOADING);
			heapFileRepository.save(entity);

			return entity;
		});

		updateProgress();

		return result;
	}

	private void updateProgress() {
		var progress = Math.round(100 * (progressCurrent / (double) progressLimit));
		transactionOperations.executeWithoutResult(st -> {
			var entity = heapFileRepository.findById(entityId).orElseThrow();
			if (entity.getLoadProgress() == null || progress != Math.round(100 * entity.getLoadProgress())) {
				entity.setLoadProgress(progress / 100f);
				heapFileRepository.save(entity);
			}
		});
	}
}
