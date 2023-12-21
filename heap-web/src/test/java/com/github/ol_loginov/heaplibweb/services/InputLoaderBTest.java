package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.TestTool;
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest;
import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {InputLoaderBTest.TestContext.class})
@Disabled("manual run")
public class InputLoaderBTest extends DatabaseTest {
	public static class TestContext {
		@Bean
		@Primary
		public InputFilesManager inputLoadWorkFactory() {
			return mock(InputFilesManager.class);
		}
	}

	@Inject
	private HeapFileRepository heapFileRepository;
	@Inject
	private InputFilesManager inputFilesManager;
	@Inject
	private ObjectProvider<InputLoader> inputLoaderProvider;
	@TempDir
	private Path tempDir;

	@Test
	public void load_1703107559883() {
		var inputFileName = "1703107559883.hprof";
		var inputFileDump = tempDir.resolve(inputFileName);
		TestTool.copyResourceTo("heapdumps/remote-jdbc-1703107559883.sanitized.hprof", inputFileDump);
		when(inputFilesManager.getInputFile(inputFileName)).thenReturn(inputFileDump);

		var heapFile = new HeapFile();
		heapFile.setId(1);
		heapFile.setRelativePath(inputFileName);
		when(heapFileRepository.findById(heapFile.getId())).thenReturn(Optional.of(heapFile));

		var work = inputLoaderProvider.getObject();
		work.withEntityId(heapFile.getId());
		work.run();
	}
}
