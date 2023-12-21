package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.HeapFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InputFilesManager {
	Path getInputFilesFolder();

	Path getInputFile(String relativePath);

	List<InputFile> listInputFiles() throws IOException;

	List<HeapFile> listLoadedFiles();

	void createLoad(String relativePath);
}
