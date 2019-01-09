package nl.myndocs.oauth2.grant

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.RefreshTokenRequest
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class RefreshTokenGrantAuthorizerTest {
    @MockK
    lateinit var identityService: IdentityService
    @MockK
    lateinit var clientService: ClientService
    @RelaxedMockK
    lateinit var tokenStore: TokenStore

    @InjectMockKs
    lateinit var authorizer: RefreshTokenGrantAuthorizer

    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val refreshToken = "refresh-token"
    val username = "foo-user"
    val scope = "scope1"
    val scopes = setOf(scope)

    val refreshTokenRequest = RefreshTokenRequest(
        clientId,
        clientSecret,
        refreshToken
    )

    @Test
    fun validRefreshToken() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))
        val token = RefreshToken("test", Instant.now(), username, clientId, scopes)
        val identity = Identity(username)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.refreshToken(refreshToken) } returns token
        every { identityService.identityOf(client, username) } returns identity

        authorizer.authorize(refreshTokenRequest)
    }

    @Test
    fun missingRefreshToken() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        val refreshTokenRequest = RefreshTokenRequest(
            clientId,
            clientSecret,
            null
        )

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) { authorizer.authorize(refreshTokenRequest) }
    }

    @Test
    fun storedClientDoesNotMatchRequestedException() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))
        val token = RefreshToken("test", Instant.now(), username, "wrong-client", scopes)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.refreshToken(refreshToken) } returns token

        Assertions.assertThrows(
            InvalidGrantException::class.java
        ) { authorizer.authorize(refreshTokenRequest) }
    }
}