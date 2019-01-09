package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.ClientRequest
import kotlin.reflect.KClass

class MockAuthorizer(
    override val shouldVerifyUnverifiedClient: Boolean,
    override val shouldValidateScopes: Boolean
) : GrantAuthorizer<MockClientRequest>{
    override val clientRequestClass: KClass<MockClientRequest>
        get() = MockClientRequest::class

    override fun authorize(clientRequest: MockClientRequest): TokenInfo {
        return clientRequest.validate()
    }
}

class MockClientRequest(
    override val clientId: String?,
    override val clientSecret: String?,
    val validate: () -> TokenInfo
) : ClientRequest