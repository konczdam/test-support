package hu.konczdam.testsupport.testcontainers

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer

/**
 * Test configuration for local Docker-based PostgreSQL database.
 *
 * Activate by setting `use-embedded-database=true` in test properties.
 * The container will be reused across tests for better performance.
 */
@TestConfiguration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ["use-embedded-database"], havingValue = "true", matchIfMissing = false)
class LocalDockerDbConfig {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer =
        PostgreSQLContainer("postgres:17.0-alpine3.20")
            .withReuse(true)
}
