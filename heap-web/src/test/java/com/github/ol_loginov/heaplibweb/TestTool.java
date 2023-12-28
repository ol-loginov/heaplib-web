package com.github.ol_loginov.heaplibweb;

import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestTool {
	public static TransactionOperations withoutTransaction() {
		return new TransactionOperations() {
			@Override
			public <T> T execute(@NonNull TransactionCallback<T> action) throws TransactionException {
				return action.doInTransaction(new SimpleTransactionStatus());
			}
		};
	}

	public static File getResourceFile(String sourceResource) throws FileNotFoundException {
		return ResourceUtils.getFile("classpath:" + sourceResource);
	}

	public static void copyResourceTo(String sourceResource, Path targetFile) throws IOException {
		try (var is = TestTool.class.getClassLoader().getResourceAsStream(sourceResource)) {
			Files.copy(Objects.requireNonNull(is, "resource '" + sourceResource + "' is not available"), targetFile);
		}
	}

	public static <T> OngoingStubbing<T> _when(T methodCall) {
		return Mockito.when(methodCall);
	}
}
