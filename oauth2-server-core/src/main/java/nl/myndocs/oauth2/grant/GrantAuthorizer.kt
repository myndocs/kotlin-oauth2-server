package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.ClientRequest
import kotlin.reflect.KClass

interface GrantAuthorizer<TGrantRequest : ClientRequest> {
    val INVALID_REQUEST_FIELD_MESSAGE: String
        get() = "'%s' field is missing"
    val clientRequestClass: KClass<TGrantRequest>

    val shouldVerifyUnverifiedClient: Boolean
    val shouldValidateScopes: Boolean

    /**
     * @throws InvalidIdentityException
     * @throws InvalidClientException
     * @throws InvalidScopeException
     */
    fun authorize(clientRequest: TGrantRequest): TokenInfo
}
