package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.token.TokenStore
import kotlin.reflect.KClass

class AuthorizationCodeGrantAuthorizer(
    private val clientService: ClientService,
    private val identityService: IdentityService,
    private val tokenStore: TokenStore
) : GrantAuthorizer<AuthorizationCodeRequest> {
    override val clientRequestClass: KClass<AuthorizationCodeRequest>
        get() = AuthorizationCodeRequest::class
    override val shouldVerifyUnverifiedClient: Boolean
        get() = true
    override val shouldValidateScopes: Boolean
        get() = false

    override fun authorize(clientRequest: AuthorizationCodeRequest): TokenInfo {
        if (clientRequest.code == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("code"))
        }

        if (clientRequest.redirectUri == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
        }

        val consumeCodeToken = tokenStore.consumeCodeToken(clientRequest.code)
            ?: throw InvalidGrantException()


        if (consumeCodeToken.redirectUri != clientRequest.redirectUri || consumeCodeToken.clientId != clientRequest.clientId) {
            throw InvalidGrantException()
        }

        val client = clientService.clientOf(consumeCodeToken.clientId)!!

        return TokenInfo(
            identity = identityService.identityOf(client, consumeCodeToken.username),
            client = client,
            scopes = consumeCodeToken.scopes
        )
    }
}