package nl.myndocs.oauth2.grant

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.TokenStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class AuthorizationCodeGrantAuthorizerTest {
    @MockK
    lateinit var identityService: IdentityService
    @MockK
    lateinit var clientService: ClientService
    @RelaxedMockK
    lateinit var tokenStore: TokenStore

    @InjectMockKs
    lateinit var authorizer: AuthorizationCodeGrantAuthorizer

    val clientId = "client-foo"
    val clientSecret = "client-bar"
    val code = "user-foo"
    val redirectUri = "http://foo.localhost"
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

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        val identity = Identity(username)
        val codeToken = CodeToken(code, Instant.now(), username, clientId, redirectUri, requestScopes)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { identityService.identityOf(client, username) } returns identity
        every { tokenStore.consumeCodeToken(code) } returns codeToken

        authorizer.authorize(authorizationCodeRequest)
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

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) { authorizer.authorize(authorizationCodeRequest) }
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

        Assertions.assertThrows(
            InvalidRequestException::class.java
        ) { authorizer.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidRedirectUriException() {
        val wrongRedirectUri = ""
        val requestScopes = setOf("scope1")

        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))
        val codeToken = CodeToken(code, Instant.now(), username, clientId, wrongRedirectUri, requestScopes)

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns codeToken

        Assertions.assertThrows(
            InvalidGrantException::class.java
        ) { authorizer.authorize(authorizationCodeRequest) }
    }

    @Test
    fun invalidCodeException() {
        val client = Client(clientId, setOf("scope1", "scope2"), setOf(), setOf(AuthorizedGrantType.AUTHORIZATION_CODE))

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true
        every { tokenStore.consumeCodeToken(code) } returns null

        Assertions.assertThrows(
            InvalidGrantException::class.java
        ) { authorizer.authorize(authorizationCodeRequest) }
    }

}