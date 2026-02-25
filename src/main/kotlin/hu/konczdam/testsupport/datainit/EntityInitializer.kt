package hu.konczdam.testsupport.datainit

import hu.konczdam.testsupport.dev.DevUserProvider
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Clock
import java.time.ZoneId

/**
 * Interface for initializing test data entities.
 *
 * Implementations should be Spring beans (typically @Service) and annotated
 * with @Initializes to declare which entity they create.
 *
 * @param T The entity type this initializer creates
 */
interface EntityInitializer<T> {
    /**
     * Initialize the entity and store it for later retrieval.
     */
    fun initialize()

    /**
     * Get the default initialized entity.
     * @return The entity initialized by this initializer
     */
    fun getDefaultEntity(): T

    /**
     * Initialize the entity with a dev/test user authentication context.
     *
     * Sets up a JWT authentication token with a fixed dev user ID,
     * calls initialize(), then restores the original authentication.
     */
    fun initializeWithAuth() {
        val clock = Clock.system(ZoneId.of("Europe/Budapest"))
        val originalAuth = SecurityContextHolder.getContext().authentication
        val jwt =
            Jwt(
                "fakeTokenValue",
                clock.instant(),
                clock.instant().plusSeconds(3600),
                mapOf("alg" to "none"),
                mapOf("sub" to DevUserProvider.DEV_USER_ID, "roles" to listOf("ROLE_ADMIN")),
            )

        // Create a custom authenticated token
        val token =
            object : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_ADMIN"))) {
                override fun getCredentials(): Any = jwt

                override fun getPrincipal(): Any = jwt.subject ?: DevUserProvider.DEV_USER_ID
            }
        token.isAuthenticated = true

        SecurityContextHolder.getContext().authentication = token

        initialize()

        SecurityContextHolder.getContext().authentication = originalAuth
    }
}
