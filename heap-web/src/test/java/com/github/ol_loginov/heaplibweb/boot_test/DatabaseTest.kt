package com.github.ol_loginov.heaplibweb.boot_test

import com.github.ol_loginov.heaplibweb.boot.RepositoryConfig
import jakarta.inject.Inject
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ExtendWith(MockitoExtension::class)
@EnableAutoConfiguration
@ActiveProfiles("local", "test")
@SpringBootTest(classes = [DatabaseTest.DatabaseTestContext::class])
@Transactional
@Rollback
abstract class DatabaseTest {
    @Configuration
    @Import(RepositoryConfig::class)
    class DatabaseTestContext {
//        @Bean
//        fun transactionTemplate(transactionManager: PlatformTransactionManager?): TransactionTemplate {
//            return TransactionTemplate(transactionManager!!)
//        }
    }

    @Inject
    lateinit var jdbc: JdbcClient
}