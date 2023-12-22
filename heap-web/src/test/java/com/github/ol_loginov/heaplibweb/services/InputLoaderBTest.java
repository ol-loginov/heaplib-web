package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.TestTool;
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest;
import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {InputLoaderBTest.TestContext.class})
@Disabled("manual run")
@Rollback(false)
public class InputLoaderBTest extends DatabaseTest {
	@Import(InputLoader.class)
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
		heapFile.setRelativePath(inputFileName);
		heapFileRepository.save(heapFile);

		var work = inputLoaderProvider.getObject();
		work.withEntityId(heapFile.getId());
		work.run();
	}
}
