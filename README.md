# Test Support Library

Shared test infrastructure for Spring Boot applications.

## Features

### 1. @ServiceConnection Testcontainers Setup
Simplified Testcontainers configuration using Spring Boot's `@ServiceConnection`.

- Enable with `use-embedded-database=true` in test properties
- Automatically configures DataSource
- Container reuse for better performance

### 2. GitHub Workflow Profile
Automatic profile management for different environments.

- Always adds `test` profile
- Conditionally adds `github_workflow` profile when `-Denvironment=github_workflow`
- Disables embedded database in CI (uses GitHub Services)

### 3. Test Data Initialization Framework
Annotation-driven test data setup with dependency resolution.

- `@Testing(Entity::class)` on test methods
- `@Initializes(Entity::class)` on initializer classes
- `@DependsOn(OtherEntity::class)` for dependencies
- Automatic authentication context setup

## Installation

```xml
<dependency>
    <groupId>hu.konczdam</groupId>
    <artifactId>test-support</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Basic Integration Test

```kotlin
@SpringBootTest
class MyIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testSomething() {
        // Test with embedded PostgreSQL
    }
}
```

### Test Data Initialization

1. **Create an Initializer**:
```kotlin
@Service
@Initializes(Office::class)
class OfficeInitializer(
    private val repository: OfficeRepository,
) : EntityInitializer<Office> {
    companion object {
        lateinit var lazyDefaultEntity: Office
    }

    override fun initialize() {
        lazyDefaultEntity = repository.save(Office(name = "Test Office"))
    }

    override fun getDefaultEntity(): Office = lazyDefaultEntity
}
```

2. **Use in Tests**:
```kotlin
@Test
@Testing(Office::class)
fun testOfficeUpdate() {
    val office = OfficeInitializer.lazyDefaultEntity
    // Test code here
}
```

### With Dependencies

```kotlin
@Service
@Initializes(Invoice::class)
@DependsOn([Office::class, Client::class])
class InvoiceInitializer(
    private val repository: InvoiceRepository,
) : EntityInitializer<Invoice> {
    // Office and Client will be initialized first
}
```

## Configuration

### application-test.properties
```properties
use-embedded-database=true
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:1080/
```

### application-github_workflow.properties
```properties
use-embedded-database=false
```

## Running Tests

**Local with Docker:**
```bash
./mvnw test
```

**GitHub Actions (without Docker):**
```bash
./mvnw test -Denvironment=github_workflow
```

## Implementation

### DevUserProvider

Implement this interface to provide test user IDs:

```kotlin
@Profile("test")
@Service
class DevUserProviderImpl : DevUserProvider {
    override val devUserId = "00000000-0000-0000-0000-000000000000"
}
```

## Package Structure

```
hu.konczdam.testsupport/
├── testcontainers/     - Testcontainers configuration
├── profile/            - Profile management
├── datainit/           - Test data initialization framework
└── dev/                - Dev user provider interface
```
