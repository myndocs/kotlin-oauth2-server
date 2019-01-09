package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.scope.ScopeParser
import kotlin.reflect.KClass

class PasswordGrantAuthorizer(
        private val clientService: ClientService,
        private val identityService: IdentityService
) : GrantAuthorizer<PasswordGrantRequest> {
    override val shouldVerifyUnverifiedClient: Boolean
        get() = true
    override val shouldValidateScopes: Boolean
        get() = true
    override val clientRequestClass: KClass<PasswordGrantRequest>
        get() = PasswordGrantRequest::class

    override fun authorize(clientRequest: PasswordGrantRequest): TokenInfo {
        if (clientRequest.username == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
        }

        if (clientRequest.password == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
        }

        val requestedClient = clientService.clientOf(clientRequest.clientId!!) ?: throw InvalidClientException()
        val requestedIdentity = identityService.identityOf(requestedClient, clientRequest.username)

        if (
                requestedIdentity == null
                || !identityService.validCredentials(requestedClient, requestedIdentity, clientRequest.password)
        ) {
            throw InvalidIdentityException()
        }

        val requestedScopes = ScopeParser.parseScopes(clientRequest.scope)

        return TokenInfo(
                identity = requestedIdentity,
                client = requestedClient,
                scopes = requestedScopes
        )
    }
}