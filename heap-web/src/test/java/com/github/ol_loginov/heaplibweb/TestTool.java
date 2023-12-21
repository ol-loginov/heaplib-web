package com.github.ol_loginov.heaplibweb;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestTool {
    public static TransactionOperations withoutTransaction() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> action) throws TransactionException {
                return action.doInTransaction(new SimpleTransactionStatus());
            }
        };
    }

    public static void copyResourceTo(String sourceResource, Path targetFile) {
        try (var is = TestTool.class.getClassLoader().getResourceAsStream(sourceResource)) {
            Files.copy(Objects.requireNonNull(is, "resource '" + sourceResource + "' is not available"), targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
