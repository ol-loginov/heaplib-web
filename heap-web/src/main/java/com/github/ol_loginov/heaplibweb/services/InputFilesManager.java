package com.github.ol_loginov.heaplibweb.services;

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
    List<InputFile> listInputFiles() throws IOException;
}

@Service
@Slf4j
class InputFilesManagerImpl implements InitializingBean, InputFilesManager {
    @Value("${app.inputFilesFolder:.}")
    private Path storeFolder;

    @Override
    public void afterPropertiesSet() {
        storeFolder = storeFolder.toAbsolutePath();
        log.info("{}", Map.of(
                "storeFolder", storeFolder));
    }

    @Override
    public List<InputFile> listInputFiles() throws IOException {
        try (var stream = Files.list(storeFolder)) {
            return stream
                    .filter(e -> e.toFile().getName().endsWith(".hprof"))
                    .map(e -> {
                        BasicFileAttributes basicAttributes = readBasicAttributes(e);
                        return new InputFile(storeFolder.relativize(e).toString(),
                                basicAttributes == null ? null : basicAttributes.lastModifiedTime().toInstant(),
                                basicAttributes == null ? null : basicAttributes.size());
                    })
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
}