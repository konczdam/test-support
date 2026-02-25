package hu.konczdam.testsupport

import hu.konczdam.testsupport.datainit.TestDataInitializationListener
import hu.konczdam.testsupport.profile.ProfileAddingContextInitializer
import hu.konczdam.testsupport.testcontainers.LocalDockerDbConfig
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
 *
 * Usage:
 * ```
 * @SpringBootTest
 * class MyIntegrationTest : BaseIntegrationTest() {
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
abstract class BaseIntegrationTest
