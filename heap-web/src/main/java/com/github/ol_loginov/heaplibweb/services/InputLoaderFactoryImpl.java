package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader;
import com.github.ol_loginov.heaplibweb.support.DaemonThreadFactory;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
class InputLoaderFactoryImpl implements InputLoaderFactory, DisposableBean {
	private final ObjectProvider<InputLoader> inputLoaderProvider;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());

	@Override
	public void destroy() {
		executor.shutdownNow();
	}

	@Override
	public void add(int inputFileLoadId) {
		var task = inputLoaderProvider
			.getObject()
			.withEntityId(inputFileLoadId);
		executor.submit(task);
	}
}
