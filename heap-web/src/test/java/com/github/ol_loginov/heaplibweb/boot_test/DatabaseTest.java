package com.github.ol_loginov.heaplibweb.boot_test;

import com.github.ol_loginov.heaplibweb.boot.RepositoryConfig;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = DatabaseTest.DatabaseTestContext.class)
@Transactional
@Rollback
public abstract class DatabaseTest {
	@Configuration
	@Import({RepositoryConfig.class})
	static class DatabaseTestContext {
	}

	@Inject
	protected SessionFactory sessionFactory;
}
