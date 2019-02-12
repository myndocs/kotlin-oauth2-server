package nl.myndocs.oauth2

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.authorize
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.response.AccessTokenResponder
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.Converters
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class ClientCredentialsTokenServiceTest {
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
    @MockK
    lateinit var accessTokenResponder: AccessTokenResponder

    lateinit var grantingCall: GrantingCall

    @BeforeEach
    fun initialize() {
        grantingCall = object : GrantingCall {
            override val callContext = this@ClientCredentialsTokenServiceTest.callContext
            override val identityService = this@ClientCredentialsTokenServiceTest.identityService
            override val clientService = this@ClientCredentialsTokenServiceTest.clientService
            override val tokenStore = this@ClientCredentialsTokenServiceTest.tokenStore
            override val converters = Converters(
                    this@ClientCredentialsTokenServiceTest.accessTokenConverter,
                    this@ClientCredentialsTokenServiceTest.refreshTokenConverter,
                    this@ClientCredentialsTokenServiceTest.codeTokenConverter
            )
            override val accessTokenResponder = this@ClientCredentialsTokenServiceTest.accessTokenResponder
        }
    }
    private val clientId = "client-foo"
    private val clientSecret = "client-secret"
    private val scope = "scope1"
    private val scopes = setOf(scope)
    private val clientCredentialsRequest = ClientCredentialsRequest(clientId, clientSecret, scope)

    @Test
    fun validClientCredentialsGrant() {
        val client = Client(clientId, emptySet(), emptySet(), setOf(AuthorizedGrantType.CLIENT_CREDENTIALS))
        val refreshToken = RefreshToken("test", Instant.now(), null, clientId, scopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), null, clientId, scopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { refreshTokenConverter.convertToToken(null, clientId, scopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(null, clientId, scopes, refreshToken) } returns accessToken

        grantingCall.authorize(clientCredentialsRequest)

        verify { tokenStore.storeAccessToken(accessToken) }
    }

    @Test
    fun nonExistingClientException() {
        every { clientService.clientOf(clientId) } returns null

        Assertions.assertThrows(
            InvalidClientException::class.java
        ) { grantingCall.authorize(clientCredentialsRequest) }
    }

    @Test
    fun invalidClientException() {
        val client = Client(clientId, emptySet(), emptySet(), setOf(AuthorizedGrantType.CLIENT_CREDENTIALS))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns false

        Assertions.assertThrows(
            InvalidClientException::class.java
        ) { grantingCall.authorize(clientCredentialsRequest) }
    }

    @Test
    fun clientScopesAsFallback() {
        val clientCredentialsRequest = ClientCredentialsRequest(
            clientId,
            clientSecret,
            null
        )

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.CLIENT_CREDENTIALS))
        val requestScopes = setOf("scope1", "scope2")
        val refreshToken = RefreshToken("test", Instant.now(), null, clientId, requestScopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), null, clientId, requestScopes, refreshToken)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { refreshTokenConverter.convertToToken(null, clientId, requestScopes) } returns refreshToken
        every { accessTokenConverter.convertToToken(null, clientId, requestScopes, refreshToken) } returns accessToken

        grantingCall.authorize(clientCredentialsRequest)
    }
}