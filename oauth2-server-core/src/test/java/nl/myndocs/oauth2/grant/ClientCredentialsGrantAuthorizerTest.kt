package nl.myndocs.oauth2.grant

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class ClientCredentialsGrantAuthorizerTest {
    @MockK
    lateinit var clientService: ClientService

    @InjectMockKs
    lateinit var authorizer: ClientCredentialsGrantAuthorizer

    private val clientId = "client-foo"
    private val clientSecret = "client-secret"
    private val scope = "scope1"
    private val scopes = setOf(scope)
    private val clientCredentialsRequest = ClientCredentialsRequest(clientId, clientSecret, scope)

    @Test
    fun validClientCredentialsGrant() {
        val client = Client(clientId, emptySet(), emptySet(), setOf(AuthorizedGrantType.CLIENT_CREDENTIALS))

        every { clientService.clientOf(clientId) } returns client
        every { clientService.validClient(client, clientSecret) } returns true

        authorizer.authorize(clientCredentialsRequest)
    }

    @Test
    fun nonExistingClientException() {
        every { clientService.clientOf(clientId) } returns null

        Assertions.assertThrows(
            InvalidClientException::class.java
        ) { authorizer.authorize(clientCredentialsRequest) }
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

        authorizer.authorize(clientCredentialsRequest)
    }
}