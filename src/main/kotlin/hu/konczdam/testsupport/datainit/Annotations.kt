package hu.konczdam.testsupport.datainit

import kotlin.reflect.KClass

/**
 * Annotation for test methods that declare which entity they are testing.
 *
 * Example usage:
 * ```
 * @Testing(Office::class)
 * fun testOfficeCreation() { ... }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Testing(
    val value: KClass<*>,
)

/**
 * Annotation that indicates which entity a particular initializer creates.
 *
 * Applied to implementations of EntityInitializer to declare the entity type.
 *
 * Example usage:
 * ```
 * @Service
 * @Initializes(Office::class)
 * class OfficeInitializer : EntityInitializer<Office> { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Initializes(
    val value: KClass<*>,
)

/**
 * Annotation that declares dependencies of an entity.
 *
 * Applied to implementations of EntityInitializer to specify which entities
 * must be initialized before this one.
 *
 * Example usage:
 * ```
 * @Service
 * @Initializes(Invoice::class)
 * @DependsOn([Office::class, Client::class])
 * class InvoiceInitializer : EntityInitializer<Invoice> { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DependsOn(
    val value: Array<KClass<*>>,
)
