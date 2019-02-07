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
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.refresh
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.RefreshTokenRequest
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
internal class RefreshTokenGrantTokenServiceTest {
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
            override val callContext = this@RefreshTokenGrantTokenServiceTest.callContext
            override val identityService = this@RefreshTokenGrantTokenServiceTest.identityService
            override val clientService = this@RefreshTokenGrantTokenServiceTest.clientService
            override val tokenStore = this@RefreshTokenGrantTokenServiceTest.tokenStore
            override val converters = Converters(
                    this@RefreshTokenGrantTokenServiceTest.accessTokenConverter,
                    this@RefreshTokenGrantTokenServiceTest.refreshTokenConverter,
                    this@RefreshTokenGrantTokenServiceTest.codeTokenConverter
            )
        }
    }
    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val refreshToken = "refresh-token"
    val username = "foo-user"
    val scope = "scope1"
    val scopes = setOf(scope)
    val identity = Identity(username)

    val refreshTokenRequest = RefreshTokenRequest(
            clientId,
            clientSecret,
            refreshToken
    )

    @Test
    fun validRefreshToken() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))
        val token = RefreshToken("test", Instant.now(), identity, clientId, scopes)
        val newRefreshToken = RefreshToken("new-test", Instant.now(), identity, clientId, scopes)
        val accessToken = AccessToken("test", "bearer", Instant.now(), identity, clientId, scopes, newRefreshToken)
        val identity = Identity(username)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.refreshToken(refreshToken) } returns token
        every { identityService.identityOf(client, username) } returns identity
        every { refreshTokenConverter.convertToToken(token) } returns newRefreshToken
        every { accessTokenConverter.convertToToken(identity, clientId, scopes, newRefreshToken) } returns accessToken

        grantingCall.refresh(refreshTokenRequest)


        verify { tokenStore.storeAccessToken(accessToken) }
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
        ) { grantingCall.refresh(refreshTokenRequest) }
    }

    @Test
    fun nonExistingClientException() {
        every { clientService.clientOf(clientId) } returns null

        Assertions.assertThrows(
                InvalidClientException::class.java
        ) { grantingCall.refresh(refreshTokenRequest) }
    }

    @Test
    fun invalidClientException() {
        val client = Client(clientId, setOf(), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))
        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns false

        Assertions.assertThrows(
                InvalidClientException::class.java
        ) { grantingCall.refresh(refreshTokenRequest) }
    }

    @Test
    fun storedClientDoesNotMatchRequestedException() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.REFRESH_TOKEN))
        val token = RefreshToken("test", Instant.now(), identity, "wrong-client", scopes)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.refreshToken(refreshToken) } returns token

        Assertions.assertThrows(
                InvalidGrantException::class.java
        ) { grantingCall.refresh(refreshTokenRequest) }
    }
}