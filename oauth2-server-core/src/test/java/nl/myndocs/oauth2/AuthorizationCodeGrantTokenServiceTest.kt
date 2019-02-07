package nl.myndocs.oauth2

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.authorize
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.Converters
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class AuthorizationCodeGrantTokenServiceTest {
    @MockK
    lateinit var callContext: CallContext
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

    lateinit var grantingCall: GrantingCall

    @BeforeEach
    fun initialize() {
        grantingCall = object : GrantingCall {
            override val callContext = this@AuthorizationCodeGrantTokenServiceTest.callContext
            override val identityService = this@AuthorizationCodeGrantTokenServiceTest.identityService
            override val clientService = this@AuthorizationCodeGrantTokenServiceTest.clientService
            override val tokenStore = this@AuthorizationCodeGrantTokenServiceTest.tokenStore
            override val converters = Converters(
                    this@AuthorizationCodeGrantTokenServiceTest.accessTokenConverter,
                    this@AuthorizationCodeGrantTokenServiceTest.refreshTokenConverter,
                    this@AuthorizationCodeGrantTokenServiceTest.codeTokenConverter
            )
        }
    }

    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val code = "user-foo"
    val redirectUri = "http://foo.lcoalhost"
    val username = "user-foo"
    val identity = Identity(username)

    val authorizationCodeRequest = AuthorizationCodeRequest(
            clientId,
            clientSecret,
            code,
            redirectUri
    )

    @Test
    fun validAuthorizationCodeGrant() {
        val requestScopes = setOf("scope1")

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        val identity = Identity(username)
        val codeToken = CodeToken(code, Instant.now(), identity, clientId, redirectUri, requestScopes)

        val refreshToken = RefreshToken("test", Instant.now(), identity, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), identity, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { tokenStore.consumeCodeToken(code) } returns codeToken
        every { refreshTokenConverter.convertToToken(identity, clientId, requestScopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(identity, clientId, requestScopes, refreshToken) } returns accessToken

        grantingCall.authorize(authorizationCodeRequest)
    }

    @Test
    fun nonExistingClientException() {
        every { clientService.clientOf(clientId) } returns null

        assertThrows(
                InvalidClientException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidClientException() {
        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns false

        assertThrows(
                InvalidClientException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

    @Test
    fun missingCodeException() {
        val authorizationCodeRequest = AuthorizationCodeRequest(
                clientId,
                clientSecret,
                null,
                redirectUri
        )

        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        assertThrows(
                InvalidRequestException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

    @Test
    fun missingRedirectUriException() {
        val authorizationCodeRequest = AuthorizationCodeRequest(
                clientId,
                clientSecret,
                code,
                null
        )

        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        assertThrows(
                InvalidRequestException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidRedirectUriException() {
        val wrongRedirectUri = ""
        val requestScopes = setOf("scope1")

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        val codeToken = CodeToken(code, Instant.now(), identity, clientId, wrongRedirectUri, requestScopes)

        val refreshToken = RefreshToken("test", Instant.now(), identity, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), identity, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns codeToken
        every { refreshTokenConverter.convertToToken(identity, clientId, requestScopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(identity, clientId, requestScopes, refreshToken) } returns accessToken

        assertThrows(
                InvalidGrantException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidCodeException() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns null

        assertThrows(
                InvalidGrantException::class.java
        ) { grantingCall.authorize(authorizationCodeRequest) }
    }

}