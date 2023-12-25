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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
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

import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {InputLoaderBTest.TestContext.class})
@Disabled("manual run")
@Rollback(false)
@Slf4j
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
	private HeapRepository heapRepository;
	@Inject
	private HeapRepositories heapRepositories;
	@Inject
	private InputFilesManager inputFilesManager;
	@Inject
	private ObjectProvider<InputLoader> inputLoaderProvider;
	@TempDir
	private Path tempDir;

	@Test
	public void load_1703107559883() throws OQLException {
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

	@Test
	public void runSomeOQL() throws OQLException {
		var heap = heapRepository.findAllByOrderByIdDesc().get(0);
		var heapProxy = new HeapProxy(heap, heapRepositories);

		var oql = new OQLEngine(heapProxy);
		var printObject = new OQLEngine.ObjectVisitor() {
			@Override
			public boolean visit(Object o) {
				log.info("result: {}", o);
				return false;
			}
		};

		oql.executeQuery("select a from [I a", null);
		oql.executeQuery("select a from [B a", null);
		oql.executeQuery("select a from [C a", null);
		oql.executeQuery("select a from [S a", null);
		oql.executeQuery("select a from [J a", null);
		oql.executeQuery("select a from [F a", null);
		oql.executeQuery("select a from [Z a", null);

		oql.executeQuery("select a from [java.lang.String a", printObject);
		oql.executeQuery("select a.count from java.lang.String a", null);

		oql.executeQuery("select map(heap.findClass(\"java.io.File\").fields, 'toHtml(it.name) + \" = \" + toHtml(it.signature)')", printObject);
		oql.executeQuery("select map(a.clazz.statics, 'toHtml(it)') from java.lang.String a", printObject);

		oql.executeQuery("select heap.forEachClass(function(xxx) { print(xxx.name); print(\"\\n\");})", printObject);
		oql.executeQuery("select heap.forEachObject(function(xxx) { print(xxx.id); print(\"\\n\");}, \"java.io.File\")", printObject);
	}
}
