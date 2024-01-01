package com.github.ol_loginov.heaplibweb.hprof

import jakarta.inject.Inject
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.test.context.ActiveProfiles
import kotlin.io.path.Path

@ActiveProfiles("local", "test", "test-local")
@SpringBootTest(classes = [HprofStreamBTest.TestContext::class])
class HprofStreamBTest {
    class TestContext

    @Inject
    private lateinit var environment: Environment

    @Test
    @Disabled("manual execution")
    fun scrollHugeDump() {
        val hugeDumpFile = Path(environment.get("test.huge-dump") ?: throw IllegalStateException("expected 'test.huge-dump' to have a file path"))
        val hprof = HprofStream(hugeDumpFile)
        hprof.scanJavaClasses()
    }
}