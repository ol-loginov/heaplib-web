package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.TestTool;
import com.github.ol_loginov.heaplibweb.boot_test.DatabaseTest;
import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepositories;
import com.github.ol_loginov.heaplibweb.repository.heap.HeapRepository;
import com.github.ol_loginov.heaplibweb.services.loaders.InputLoader;
import com.github.ol_loginov.heaplibweb.services.proxies.HeapProxy;
import jakarta.inject.Inject;
import jdk.test.lib.hprof.HprofParser;
import jdk.test.lib.hprof.model.Snapshot;
import jdk.test.lib.hprof.parser.Reader;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.visualvm.lib.jfluid.heap.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.modules.profiler.oql.engine.api.OQLException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestumDumpBTest.TestContext.class})
@Slf4j
public class TestumDumpBTest extends DatabaseTest {
	@Import(InputLoader.class)
	public static class TestContext {
		@Bean
		@Primary
		public InputFilesManager inputLoadWorkFactory() {
			return mock(InputFilesManager.class);
		}
	}

	private final String inputFileName = "heapdumps/testum-1703615978111.hprof";
	@TempDir
	private Path tempDir;
	@Inject
	private InputFilesManager inputFilesManager;
	@Inject
	private HeapRepository heapRepository;
	@Inject
	private HeapRepositories heapRepositories;
	@Inject
	private HeapFileRepository heapFileRepository;
	@Inject
	private ObjectProvider<InputLoader> inputLoaderProvider;

	@Test
	@Rollback(false)
	public void load() throws Exception {
		var inputFileDump = tempDir.resolve(inputFileName);
		Files.createDirectories(inputFileDump.getParent());
		TestTool.copyResourceTo(inputFileName, inputFileDump);
		when(inputFilesManager.getInputFile(inputFileName)).thenReturn(inputFileDump);
/*
		try (Snapshot snapshot = Reader.readFile(inputFileDump.toFile().getAbsolutePath(), false, 0)) {
			System.out.println("Snapshot read, resolving...");
			snapshot.resolve(false);
			var testumMain = snapshot.findClass("testum.Main");
			var testumClassA_Derived = snapshot.findClass("testum.ClassA_Derived");
			System.out.println("Snapshot resolved.");
		}
*/
/*

		var heap = org.graalvm.visualvm.lib.jfluid.heap.HeapFactory.createHeap(inputFileDump.toFile());
		var heapClasses = new ArrayList<Instance>();
		var heapIt = heap.getAllInstancesIterator();
		while (heapIt.hasNext()) {
			heapClasses.add(heapIt.next());
		}
*/


		var heapFile = new HeapFile();
		heapFile.setRelativePath(inputFileName);
		heapFileRepository.save(heapFile);

		var work = inputLoaderProvider.getObject();
		work.withEntityId(heapFile.getId());
		work.run();
	}

	@Test
	public void oql() throws OQLException {
		var heapFile = heapFileRepository.findFirstByRelativePathOrderByIdDesc(inputFileName).orElseThrow();
		var heap = heapRepository.findOneByFile(heapFile).orElseThrow();

		var heapProxy = new HeapProxy(heap, heapRepositories);
		var oql = new OQLEngine(heapProxy);

		oql.executeQuery("select a from testum.ClassA_Derived a", v -> {

			return false;
		});
	}
}
