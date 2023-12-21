package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.InputFileLoad;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoadRepository;
import com.github.ol_loginov.heaplibweb.repository.InputFileStatus;
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
public class InputLoadWork implements Runnable {
	private final TransactionOperations transactionOperations;
	private final InputFileLoadRepository inputFileLoadRepository;
	private final InputFilesManager inputLoadWorkFactory;

	private int entityId;

	private long bufferSizeMb = 100L;

	private long progressLimit = 1;
	private long progressCurrent = 0;

	public InputLoadWork withEntityId(int entityId) {
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
		var entity = loadEntity();
		var dump = inputLoadWorkFactory.getInputFile(entity.getRelativePath()).toFile();

		Heap heap;
		if (!HeapFactory2.canBeMemMapped(dump)) {
			heap = HeapFactory2.createFastHeap(dump, bufferSizeMb << 20);
		} else {
			heap = HeapFactory2.createFastHeap(dump);
		}

		var summary = heap.getSummary();
		var allClasses = heap.getAllClasses();

		progressLimit = allClasses.size() + summary.getTotalAllocatedInstances();
		progressCurrent = 0;

		heap.getAllClasses().forEach(javaClass -> {

		});
	}

	private InputFileLoad loadEntity() {
		var result = transactionOperations.execute(st -> {
			var entity = inputFileLoadRepository.findById(entityId).orElseThrow();
			entity.setStatus(InputFileStatus.LOADING);
			inputFileLoadRepository.save(entity);

			return entity;
		});

		updateProgress();

		return result;
	}

	private void updateProgress() {
		var progress = Math.round(100 * (progressCurrent / (double) progressLimit));
		transactionOperations.executeWithoutResult(st -> {
			var entity = inputFileLoadRepository.findById(entityId).orElseThrow();
			if (entity.getLoadProgress() == null || progress != Math.round(100 * entity.getLoadProgress())) {
				entity.setLoadProgress(progress / 100f);
				inputFileLoadRepository.save(entity);
			}
		});
	}
}
