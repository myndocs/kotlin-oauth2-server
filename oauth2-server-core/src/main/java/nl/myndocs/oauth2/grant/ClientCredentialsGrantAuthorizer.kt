package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.ClientCredentialsRequest
import nl.myndocs.oauth2.scope.ScopeParser
import kotlin.reflect.KClass

class ClientCredentialsGrantAuthorizer(
    private val clientService: ClientService
) : GrantAuthorizer<ClientCredentialsRequest> {
    override val clientRequestClass: KClass<ClientCredentialsRequest>
        get() = ClientCredentialsRequest::class
    override val shouldVerifyUnverifiedClient: Boolean
        get() = true
    override val shouldValidateScopes: Boolean
        get() = false

    override fun authorize(clientRequest: ClientCredentialsRequest): TokenInfo {
        val requestedClient = clientService.clientOf(clientRequest.clientId!!)
            ?: throw InvalidClientException()

        val scopes = clientRequest.scope
            ?.let { ScopeParser.parseScopes(it).toSet() }
            ?: requestedClient.clientScopes

        return TokenInfo(
            identity = null,
            client = requestedClient,
            scopes = scopes
        )
    }
}