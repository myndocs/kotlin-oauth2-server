package nl.myndocs.oauth2

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class AuthorizationCodeGrantTokenServiceTest {
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

    @InjectMockKs
    lateinit var tokenService: TokenService

    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val code = "user-foo"
    val redirectUri = "http://foo.lcoalhost"
    val username = "user-foo"

    val authorizationCodeRequest = AuthorizationCodeRequest(
            clientId,
            clientSecret,
            code,
            redirectUri
    )

    @Test
    fun validAuthorizationCodeGrant() {
        val requestScopes = setOf("scope1")

        val client = Client(clientId, setOf("scope1", "scope2"), setOf())
        val identity = Identity(username)
        val codeToken = CodeToken(code, Instant.now(), username, clientId, redirectUri, requestScopes)

        val refreshToken = RefreshToken("test", Instant.now(), username, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), username, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { tokenStore.consumeCodeToken(code) } returns codeToken
        every { refreshTokenConverter.convertToToken(username, clientId, requestScopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(username, clientId, requestScopes, refreshToken) } returns accessToken

        tokenService.authorize(authorizationCodeRequest)
    }

    @Test
    fun nonExistingClientException() {
        every { clientService.clientOf(clientId) } returns null

        assertThrows(
                InvalidClientException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidClientException() {
        val client = Client(clientId, setOf(), setOf())
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns false

        assertThrows(
                InvalidClientException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

    @Test
    fun missingCodeException() {
        val authorizationCodeRequest = AuthorizationCodeRequest(
                clientId,
                clientSecret,
                null,
                redirectUri
        )

        val client = Client(clientId, setOf(), setOf())
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        assertThrows(
                InvalidRequestException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

    @Test
    fun missingRedirectUriException() {
        val authorizationCodeRequest = AuthorizationCodeRequest(
                clientId,
                clientSecret,
                code,
                null
        )

        val client = Client(clientId, setOf(), setOf())
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        assertThrows(
                InvalidRequestException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidRedirectUriException() {
        val wrongRedirectUri = ""
        val requestScopes = setOf("scope1")

        val client = Client(clientId, setOf("scope1", "scope2"), setOf())
        val codeToken = CodeToken(code, Instant.now(), username, clientId, wrongRedirectUri, requestScopes)

        val refreshToken = RefreshToken("test", Instant.now(), username, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), username, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns codeToken
        every { refreshTokenConverter.convertToToken(username, clientId, requestScopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(username, clientId, requestScopes, refreshToken) } returns accessToken

        assertThrows(
                InvalidGrantException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidCodeException() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf())

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns null

        assertThrows(
                InvalidGrantException::class.java
        ) { tokenService.authorize(authorizationCodeRequest) }
    }

}