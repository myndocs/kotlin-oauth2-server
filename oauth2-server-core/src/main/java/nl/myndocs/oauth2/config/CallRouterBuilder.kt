package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.grant.*
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.CallContext

internal object CallRouterBuilder {
    class Configuration {
        var tokenEndpoint: String = "/oauth/token"
        var authorizeEndpoint: String = "/oauth/authorize"
        var tokenInfoEndpoint: String = "/oauth/tokeninfo"
        var tokenInfoCallback: (TokenInfo) -> Map<String, Any?> = { tokenInfo ->
            mapOf(
                    "username" to tokenInfo.identity?.username,
                    "scopes" to tokenInfo.scopes
            ).filterValues { it != null }
        }
        var granters: List<GrantingCall.() -> Granter> = listOf()
    }

    fun build(configuration: Configuration, grantingCallFactory: (CallContext) -> GrantingCall, authorizerFactory: (CallContext) -> Authorizer) = CallRouter(
            configuration.tokenEndpoint,
            configuration.authorizeEndpoint,
            configuration.tokenInfoEndpoint,
            configuration.tokenInfoCallback,
            listOf<GrantingCall.() -> Granter>(
                    { grantPassword() },
                    { grantAuthorizationCode() },
                    { grantClientCredentials() },
                    { grantRefreshToken() }
            ) + configuration.granters,
            grantingCallFactory,
            authorizerFactory
    )
}