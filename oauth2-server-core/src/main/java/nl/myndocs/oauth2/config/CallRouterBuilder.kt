package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.*
import nl.myndocs.oauth2.grant.*
import nl.myndocs.oauth2.identity.TokenInfo

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
        var tokenService: TokenService? = null
        var granters: List<GrantingCall.() -> Granter> = listOf()
    }

    fun build(configurer: Configuration.() -> Unit): CallRouter {
        val configuration = Configuration()
        configurer(configuration)

        return build(configuration)
    }

    fun build(configuration: Configuration) = CallRouter(
            configuration.tokenService!!,
            configuration.tokenEndpoint,
            configuration.authorizeEndpoint,
            configuration.tokenInfoEndpoint,
            configuration.tokenInfoCallback,
            listOf<GrantingCall.() -> Granter>(
                    { grantPassword() },
                    { grantAuthorizationCode() },
                    { grantClientCredentials() },
                    { grantRefreshToken() }
            ) + configuration.granters
    )
}