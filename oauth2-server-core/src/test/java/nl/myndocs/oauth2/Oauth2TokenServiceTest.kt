package nl.myndocs.oauth2

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.grant.GrantAuthorizer
import nl.myndocs.oauth2.grant.MockAuthorizer
import nl.myndocs.oauth2.grant.MockClientRequest
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class Oauth2TokenServiceTest {
    @MockK
    lateinit var identityService: IdentityService
    @MockK
    lateinit var clientService: ClientService
    @RelaxedMockK
    lateinit var tokenStore: TokenStore
    @MockK
    lateinit var accessTokenConverter: AccessTokenConverter
    @MockK
    lateinit var refreshTokenConverter: RefreshTokenConverter
    @MockK
    lateinit var codeTokenConverter: CodeTokenConverter
    val grantTypeAuthorizers = mutableMapOf<String, GrantAuthorizer<*>>()

    @InjectMockKs
    lateinit var tokenService: Oauth2TokenService

    private val clientId = "client-foo"
    private val clientSecret = "client-secret"
    private val scope = "scope1"
    private val scopes = setOf(scope)

    @Test
    fun missingClientId() {
        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(null, null) { throw RuntimeException("This should never happen") }

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) {
            tokenService.authorize("mock", mockRequest)
        }
    }

    @Test
    fun missingClientSecret() {
        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(clientId, null) { throw RuntimeException("This should never happen") }

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) {
            tokenService.authorize("mock", mockRequest)
        }
    }

    @Test
    fun nonExistingClient() {
        every { clientService.clientOf(clientId) } returns null
        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(clientId, "not the client secret") { throw RuntimeException("This should never happen") }

        Assertions.assertThrows(
            InvalidClientException::class.java
        ) {
            tokenService.authorize("mock", mockRequest)
        }
    }

    @Test
    fun invalidClient() {
        val client = Client(clientId, scopes, emptySet(), emptySet())
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns false

        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(clientId, clientSecret) { throw RuntimeException("This should never happen") }

        Assertions.assertThrows(
            InvalidClientException::class.java
        ) {
            tokenService.authorize("mock", mockRequest)
        }
    }

    @Test
    fun notAllowedScopes() {
        val client = Client(clientId, scopes, emptySet(), setOf("mock"))
        val identity = Identity("foo")
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.allowedScopes(client, identity, scopes) } returns emptySet()

        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(clientId, clientSecret) {
            TokenInfo(identity, client, scopes)
        }

        Assertions.assertThrows(
            InvalidScopeException::class.java
        ) {
            tokenService.authorize("mock", mockRequest)
        }
    }

    @Test
    fun convertsAndStoresAccessToken() {
        val client = Client(clientId, scopes, emptySet(), setOf("mock"))
        val identity = Identity("foo")
        val expiryTime = Instant.now().plusSeconds(2)
        val refreshToken = RefreshToken(
            refreshToken = "refresh-foo",
            expireTime = expiryTime,
            clientId = clientId,
            username = identity.username,
            scopes = scopes
        )
        val accessToken = AccessToken(
            username = identity.username,
            scopes = scopes,
            clientId = clientId,
            expireTime = expiryTime,
            tokenType = "jwt",
            accessToken = "access-foo",
            refreshToken = refreshToken
        )
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.allowedScopes(client, identity, scopes) } returns scopes
        every { refreshTokenConverter.convertToToken(refreshToken.username, clientId, scopes) } returns refreshToken
        every {
            accessTokenConverter.convertToToken(
                username = identity.username,
                requestedScopes = scopes,
                clientId = clientId,
                refreshToken = refreshToken
            )
        } returns accessToken


        grantTypeAuthorizers["mock"] = MockAuthorizer(true, true)
        val mockRequest = MockClientRequest(clientId, clientSecret) {
            TokenInfo(identity, client, scopes)
        }

        val result = tokenService.authorize("mock", mockRequest)
        assertEquals(
            TokenResponse(
                accessToken = accessToken.accessToken,
                tokenType = accessToken.tokenType,
                expiresIn = 1,
                refreshToken = refreshToken.refreshToken
            ),
            result
        )

        verify { tokenStore.storeAccessToken(accessToken) }
    }
}
