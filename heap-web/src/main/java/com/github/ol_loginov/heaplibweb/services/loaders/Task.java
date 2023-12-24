package com.github.ol_loginov.heaplibweb.services.loaders;

import org.netbeans.lib.profiler.heap.HeapOperationUnsupportedException;

import java.util.function.LongSupplier;

import static com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories.NUMBER_NOT_READY;

interface Task {
	String getText();

	void run(Callback callback);

	interface Callback {

		void saveProgress(Task task, boolean force);

		default void saveProgress(Task task) {
			saveProgress(task, false);
		}
	}

	static long notReadyValueOnError(LongSupplier supplier) {
		try {
			return supplier.getAsLong();
		} catch (HeapOperationUnsupportedException e) {
			return NUMBER_NOT_READY;
		}
	}
}