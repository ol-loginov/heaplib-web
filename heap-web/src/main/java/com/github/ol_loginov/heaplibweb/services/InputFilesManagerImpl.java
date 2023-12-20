package com.github.ol_loginov.heaplibweb.services;

import com.github.ol_loginov.heaplibweb.repository.InputFileLoad;
import com.github.ol_loginov.heaplibweb.repository.InputFileLoadRepository;
import com.github.ol_loginov.heaplibweb.repository.InputFileStatus;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
class InputFilesManagerImpl implements InitializingBean, InputFilesManager {
    private final InputFileLoadRepository inputFileLoadRepository;
    private final InputLoadWorker inputLoadWorker;
    private final TransactionOperations transactionOperations;


    @Getter
    @Value("${app.inputFilesFolder:.}")
    private Path inputFilesFolder;

    @Override
    public void afterPropertiesSet() {
        inputFilesFolder = inputFilesFolder.toAbsolutePath();
        log.info("{}", Map.of(
                "storeFolder", inputFilesFolder));

        log.info("reset active loads");
        transactionOperations.executeWithoutResult(st -> {
            inputFileLoadRepository.findAllByStatusIn(List.of(InputFileStatus.LOADING, InputFileStatus.PENDING)).forEach(pending -> {
                pending.setStatus(InputFileStatus.LOADING_ERROR);
                pending.setLoadError("Has been reset on application start");
                inputFileLoadRepository.save(pending);
            });
        });
    }

    @Transactional
    @Override
    public List<InputFile> listInputFiles() throws IOException {
        var ignoreFiles = inputFileLoadRepository.findAllByStatusNotIn(List.of(InputFileStatus.LOADED, InputFileStatus.LOADING_ERROR))
                .stream()
                .map(InputFileLoad::getRelativePath)
                .collect(Collectors.toSet());

        try (var stream = Files.list(inputFilesFolder)) {
            return stream
                    .filter(e -> e.toFile().getName().endsWith(".hprof"))
                    .map(e -> {
                        BasicFileAttributes basicAttributes = readBasicAttributes(e);
                        return new InputFile(inputFilesFolder.relativize(e).toString(),
                                basicAttributes == null ? null : basicAttributes.lastModifiedTime().toInstant(),
                                basicAttributes == null ? null : basicAttributes.size());
                    })
                    .filter(e -> !ignoreFiles.contains(e.getPath()))
                    .collect(Collectors.toList());
        }
    }

    private BasicFileAttributes readBasicAttributes(Path e) {
        try {
            return Files.readAttributes(e, BasicFileAttributes.class);
        } catch (IOException ex) {
            log.warn("cannot read attributes of file {}", e);
            return null;
        }
    }

    @Override
    @Transactional
    public List<InputFileLoad> listLoads() {
        return inputFileLoadRepository.findAllByOrderByLoadStartDesc();
    }

    @Override
    public void createLoad(String relativePath) {
        var targetFile = this.inputFilesFolder.resolve(relativePath);
        if (!targetFile.toFile().exists() || !targetFile.toFile().isFile() || !targetFile.toFile().canRead()) {
            throw new IllegalArgumentException("not a valid file: " + targetFile);
        }

        var load = transactionOperations.execute(st -> {
            var entity = new InputFileLoad();
            entity.setLoadStart(Instant.now());
            entity.setStatus(InputFileStatus.PENDING);
            entity.setRelativePath(relativePath);
            return inputFileLoadRepository.save(entity);
        });
        if (load == null) {
            throw new IllegalStateException("cannot save new enity - returns null");
        }

        inputLoadWorker.add(load.getId());
    }
}