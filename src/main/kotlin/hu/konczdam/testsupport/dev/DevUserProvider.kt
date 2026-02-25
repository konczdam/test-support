package hu.konczdam.testsupport.dev

/**
 * Provider interface for dev/test user IDs used in test authentication.
 *
 * Implementations should provide fixed UUIDs for test users to ensure
 * deterministic test behavior.
 *
 * Example implementation in application:
 * ```
 * @Profile("test")
 * @Service
 * class DevUserProviderImpl : DevUserProvider {
 *     override val devUserId: String = "00000000-0000-0000-0000-000000000000"
 * }
 * ```
 */
interface DevUserProvider {
    /**
     * The primary dev user ID used for test authentication.
     * Should be a valid UUID string.
     */
    val devUserId: String

    companion object {
        /**
         * Default dev user ID used by EntityInitializer.
         * Override by providing a custom DevUserProvider bean.
         */
        const val DEV_USER_ID = "00000000-0000-0000-0000-000000000000"
    }
}
