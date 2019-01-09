package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.RefreshTokenRequest
import nl.myndocs.oauth2.token.TokenStore
import kotlin.reflect.KClass

class RefreshTokenGrantAuthorizer(
        private val clientService: ClientService,
        private val identityService: IdentityService,
        private val tokenStore: TokenStore
) : GrantAuthorizer<RefreshTokenRequest> {
    override val clientRequestClass: KClass<RefreshTokenRequest>
        get() = RefreshTokenRequest::class
    override val shouldVerifyUnverifiedClient: Boolean
        get() = true
    override val shouldValidateScopes: Boolean
        get() = false

    override fun authorize(clientRequest: RefreshTokenRequest): TokenInfo {
        if (clientRequest.refreshToken == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("refresh_token"))
        }

        val refreshToken = tokenStore.refreshToken(clientRequest.refreshToken) ?: throw InvalidGrantException()

        if (refreshToken.clientId != clientRequest.clientId) {
            throw InvalidGrantException()
        }

        val client = clientService.clientOf(refreshToken.clientId) ?: throw InvalidClientException()

        return TokenInfo(
                identity = identityService.identityOf(client, refreshToken.username!!),
                client = client,
                scopes = refreshToken.scopes
        )
    }
}