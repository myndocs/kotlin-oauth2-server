package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.RawRequest
import kotlin.reflect.KClass

abstract class RawRequestGrantAuthorizer : GrantAuthorizer<RawRequest>{
    override val clientRequestClass: KClass<RawRequest>
        get() = RawRequest::class
    override val shouldVerifyUnverifiedClient: Boolean
        get() = true
    override val shouldValidateScopes: Boolean
        get() = true

    abstract override fun authorize(clientRequest: RawRequest): TokenInfo
}