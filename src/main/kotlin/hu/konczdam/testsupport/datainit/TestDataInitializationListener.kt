package hu.konczdam.testsupport.datainit

import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * TestExecutionListener that automatically initializes test data based on method annotations.
 *
 * When a test method is annotated with @Testing(Entity::class), this listener:
 * 1. Finds the initializer for that entity
 * 2. Resolves and initializes all dependencies (via @DependsOn)
 * 3. Executes initialization with test authentication
 *
 * Usage:
 * ```
 * @Test
 * @Testing(Office::class)
 * fun testOfficeUpdate() { ... }
 * ```
 *
 * Must be registered in @TestExecutionListeners:
 * ```
 * @TestExecutionListeners(listeners = [TestDataInitializationListener::class], mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
 * ```
 */
class TestDataInitializationListener : TestExecutionListener {
    // Cache to keep track of initialized entities within a test method
    private val initializedCache = mutableSetOf<KClass<*>>()

    override fun beforeTestMethod(testContext: TestContext) {
        // Clear the cache before each test method
        initializedCache.clear()

        val method = testContext.testMethod
        method.getAnnotation(Testing::class.java)?.let { annotation ->
            val entityClass = annotation.value
            val initializer = findInitializerForClass(entityClass, testContext.applicationContext)
            initializeEntityWithDependencies(initializer, testContext.applicationContext)
        }
    }

    private fun findInitializerForClass(
        entityClass: KClass<*>,
        applicationContext: ApplicationContext,
    ): EntityInitializer<*> {
        // Check cache first
        if (entityClass in initializedCache) {
            return getInitializedEntity(entityClass, applicationContext)
        }

        val initializers = applicationContext.getBeansOfType(EntityInitializer::class.java).values
        val matchingInitializer =
            initializers.firstOrNull { initializer ->
                initializer::class.findAnnotation<Initializes>()?.value == entityClass
            }
        return matchingInitializer ?: throw IllegalStateException("No initializer found for class: ${entityClass.simpleName}")
    }

    private fun initializeEntityWithDependencies(
        initializer: EntityInitializer<*>,
        applicationContext: ApplicationContext,
    ) {
        // Retrieve the dependencies from the @DependsOn annotation
        val dependencies = findDependenciesForInitializer(initializer)

        // Initialize dependencies first (recursive)
        dependencies.forEach { dependencyEntityClass ->
            if (dependencyEntityClass !in initializedCache) {
                val dependencyInitializer = findInitializerForClass(dependencyEntityClass, applicationContext)
                initializeEntityWithDependencies(dependencyInitializer, applicationContext)
            }
        }

        // Check cache before initializing the entity itself
        if (initializer::class !in initializedCache) {
            initializer.initializeWithAuth()
            initializedCache.add(initializer::class)
        }
    }

    private fun findDependenciesForInitializer(initializer: EntityInitializer<*>): List<KClass<*>> {
        val dependsOnAnnotation = initializer::class.findAnnotation<DependsOn>()
        // Extract the entity classes from the DependsOn annotation
        return dependsOnAnnotation?.value?.toList() ?: emptyList()
    }

    private fun getInitializedEntity(
        entityClass: KClass<*>,
        applicationContext: ApplicationContext,
    ): EntityInitializer<*> {
        val initializers = applicationContext.getBeansOfType(EntityInitializer::class.java).values
        val initializedEntity =
            initializers.firstOrNull { initializer ->
                initializer::class == entityClass
            }

        return initializedEntity ?: throw IllegalStateException("Cached initializer not found for class: ${entityClass.simpleName}")
    }
}
