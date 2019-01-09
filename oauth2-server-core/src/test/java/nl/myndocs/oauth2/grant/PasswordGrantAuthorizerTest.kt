package nl.myndocs.oauth2.grant

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class PasswordGrantAuthorizerTest {
    @MockK
    lateinit var identityService: IdentityService
    @MockK
    lateinit var clientService: ClientService

    @InjectMockKs
    lateinit var authorizer: PasswordGrantAuthorizer

    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val username = "user-foo"
    val password = "password-bar"
    val scope = "scope1"
    val scopes = setOf(scope)

    val passwordGrantRequest = PasswordGrantRequest(
        clientId,
        clientSecret,
        username,
        password,
        scope
    )

    @Test
    fun validPasswordGrant() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.PASSWORD))
        val identity = Identity(username)
        val requestScopes = setOf("scope1")

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { identityService.validCredentials(client, identity, password) } returns true
        every { identityService.allowedScopes(client, identity, requestScopes) } returns scopes

        authorizer.authorize(passwordGrantRequest)
    }

    @Test
    fun missingUsernameException() {
        val passwordGrantRequest = PasswordGrantRequest(
            clientId,
            clientSecret,
            null,
            password,
            scope
        )

        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.PASSWORD))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) { authorizer.authorize(passwordGrantRequest) }
    }

    @Test
    fun missingPasswordException() {
        val passwordGrantRequest = PasswordGrantRequest(
            clientId,
            clientSecret,
            username,
            null,
            scope
        )

        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.PASSWORD))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) { authorizer.authorize(passwordGrantRequest) }
    }

    @Test
    fun invalidIdentityException() {
        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.PASSWORD))
        val identity = Identity(username)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { identityService.validCredentials(client, identity, password) } returns false

        Assertions.assertThrows(
            InvalidIdentityException::class.java
        ) { authorizer.authorize(passwordGrantRequest) }
    }

    @Test
    fun clientScopesAsFallback() {
        val passwordGrantRequest = PasswordGrantRequest(
            clientId,
            clientSecret,
            username,
            password,
            null
        )

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.PASSWORD))
        val identity = Identity(username)
        val requestScopes = setOf("scope1", "scope2")
        val refreshToken = RefreshToken("test", Instant.now(), username, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), username, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { identityService.validCredentials(client, identity, password) } returns true
        every { identityService.allowedScopes(client, identity, requestScopes) } returns requestScopes

        authorizer.authorize(passwordGrantRequest)
    }
}