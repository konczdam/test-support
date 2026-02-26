package hu.konczdam.testsupport.datainit

import hu.konczdam.testsupport.dev.DevUserProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Clock
import java.time.ZoneId

/**
 * Base class for initializing test data entities.
 *
 * Subclasses should be Spring beans (typically @Service) and annotated
 * with @Initializes to declare which entity they create.
 *
 * @param T The entity type this initializer creates
 */
abstract class EntityInitializer<T> {
    @Autowired
    protected lateinit var devUserProvider: DevUserProvider
        protected set

    /**
     * Initialize the entity and store it for later retrieval.
     */
    abstract fun initialize()

    /**
     * Get the default initialized entity.
     * @return The entity initialized by this initializer
     */
    abstract fun getDefaultEntity(): T

    /**
     * Initialize the entity with a dev/test user authentication context.
     *
     * Sets up a JWT authentication token with a dev user ID from the DevUserProvider bean,
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
                mapOf("sub" to devUserProvider.devUserId, "roles" to listOf("ROLE_ADMIN")),
            )

        // Create a custom authenticated token
        val token =
            object : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_ADMIN"))) {
                override fun getCredentials(): Any = jwt

                override fun getPrincipal(): Any = jwt.subject ?: devUserProvider.devUserId
            }
        token.isAuthenticated = true

        SecurityContextHolder.getContext().authentication = token

        initialize()

        SecurityContextHolder.getContext().authentication = originalAuth
    }
}
