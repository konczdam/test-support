package hu.konczdam.testsupport

import hu.konczdam.testsupport.datainit.TestDataInitializationListener
import hu.konczdam.testsupport.dev.DevUserProvider
import hu.konczdam.testsupport.profile.ProfileAddingContextInitializer
import hu.konczdam.testsupport.testcontainers.LocalDockerDbConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestExecutionListeners.MergeMode

/**
 * Base class for integration tests that provides:
 *
 * 1. **Testcontainers with @ServiceConnection**: Local Docker PostgreSQL when `use-embedded-database=true`
 * 2. **Automatic Profile Management**: Always adds 'test' profile, conditionally adds 'github_workflow'
 * 3. **Test Data Initialization**: Automatic entity initialization via @Testing annotation
 * 4. **Dev User Setup**: Ensures dev user is initialized before tests (override `initializeTestData()` to customize)
 *
 * Usage:
 * ```
 * @SpringBootTest
 * class MyIntegrationTest : TermenyBaseIntegrationTest() {
 *
 *     @Test
 *     @Testing(Office::class)
 *     fun testOfficeCreation() {
 *         val office = OfficeInitializer.lazyDefaultEntity
 *         // Test code here
 *     }
 * }
 * ```
 *
 * Profiles:
 * - Local tests with Docker: `./mvnw test` (requires Docker running)
 * - GitHub workflow: `./mvnw test -Denvironment=github_workflow`
 *
 * Properties in `application-test.properties`:
 * ```
 * use-embedded-database=true
 * spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:1080/
 * ```
 */
@SpringBootTest
@Import(LocalDockerDbConfig::class)
@ContextConfiguration(initializers = [ProfileAddingContextInitializer::class])
@TestExecutionListeners(
    listeners = [TestDataInitializationListener::class],
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS,
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var devUserProvider: DevUserProvider

    /**
     * Override this method to initialize test-specific data.
     * Called before each test class runs.
     * Default implementation does nothing - apps should override to create dev users, etc.
     */
    protected open fun initializeTestData() {
        // Default: does nothing
        // Apps can override to create dev users, etc.
    }

    @BeforeAll
    fun setUp() {
        initializeTestData()
    }
}
