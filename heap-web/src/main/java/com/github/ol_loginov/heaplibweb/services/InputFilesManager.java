package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.InputFileLoad;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoadRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface InputFilesManager {
    Path getInputFilesFolder();

    Path getInputFile(String relativePath);

    List<InputFile> listInputFiles() throws IOException;

    List<InputFileLoad> listLoads();

    void createLoad(String relativePath);
}
