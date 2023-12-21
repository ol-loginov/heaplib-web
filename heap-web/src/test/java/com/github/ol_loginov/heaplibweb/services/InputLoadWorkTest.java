package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.TestTool;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoad;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InputLoadWorkTest {
	@Mock
	private InputFileLoadRepository inputFileLoadRepository;
	@Mock
	private InputFilesManager inputLoadWorkFactory;
	@TempDir
	private Path tempDir;

	@Test
	public void load_1703107559883() {
		var inputFileName = "1703107559883.hprof";
		var inputFileDump = tempDir.resolve(inputFileName);
		TestTool.copyResourceTo("heapdumps/remote-jdbc-1703107559883.sanitized.hprof", inputFileDump);
		when(inputLoadWorkFactory.getInputFile(inputFileName)).thenReturn(inputFileDump);

		var inputFileLoad = new InputFileLoad();
		inputFileLoad.setId(1);
		inputFileLoad.setRelativePath(inputFileName);
		when(inputFileLoadRepository.findById(inputFileLoad.getId())).thenReturn(Optional.of(inputFileLoad));

		var work = new InputLoadWork(TestTool.withoutTransaction(), inputFileLoadRepository, inputLoadWorkFactory);
		work.withEntityId(inputFileLoad.getId());

		work.run();
	}
}
