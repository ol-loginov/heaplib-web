package com.github.ol_loginov.heaplibweb.boot_test

import com.github.ol_loginov.heaplibweb.boot.RepositoryConfig
import com.github.ol_loginov.heaplibweb.repository.HeapFile
import com.github.ol_loginov.heaplibweb.repository.HeapFileRepository
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
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
import org.springframework.transaction.support.TransactionOperations

@ExtendWith(MockitoExtension::class)
@EnableAutoConfiguration
@ActiveProfiles("local", "test", "test-local")
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
    protected lateinit var jdbc: JdbcClient
    @Inject
    protected lateinit var heapFileRepository: HeapFileRepository
    @Inject
    protected lateinit var transactionOperations: TransactionOperations

    private val heapsToDrop = mutableListOf<Int>()

    protected fun dropScopeAfterTest(heap: HeapFile?) {
        heap?.let { heapsToDrop.add(it.id) }
    }

    protected fun dropScopeAfterTest(heapId: Int) {
        heapsToDrop.add(heapId)
    }

    @AfterEach
    fun clearTest() {
        heapsToDrop.forEach { heap ->
            transactionOperations.executeWithoutResult {
                heapFileRepository.findById(heap)?.let { entity ->
                    heapFileRepository.getHeapRepositories(entity).dropTables()
                }
            }
        }
    }
}