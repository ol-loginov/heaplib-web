package com.github.ol_loginov.heaplibweb.boot_test;

import com.github.ol_loginov.heaplibweb.boot.RepositoryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"local", "test"})
@SpringBootTest(classes = {ConfigurationPropertiesAutoConfiguration.class, DatabaseTest.DatabaseTestContext.class})
@Transactional
@Rollback
public abstract class DatabaseTest {
	@Configuration
	@Import({TransactionAutoConfiguration.class, DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class, HibernateJpaAutoConfiguration.class, RepositoryConfig.class})
	static class DatabaseTestContext {
		@Bean
		public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
			return new TransactionTemplate(transactionManager);
		}
	}

	@PersistenceContext
	protected EntityManager entityManager;
}
