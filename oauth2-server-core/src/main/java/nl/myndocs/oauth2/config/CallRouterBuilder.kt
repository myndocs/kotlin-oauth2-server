package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.grant.GrantAuthorizer
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.ClientRequest

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
    }

    fun build(configurer: Configuration.() -> Unit): CallRouter {
        val configuration = Configuration()
        configurer(configuration)

        return build(configuration)
    }

    fun build(configuration: Configuration) = CallRouter(
            configuration.tokenService!!,
            configuration.tokenService!!.allowedGrantAuthorizers,
            configuration.tokenEndpoint,
            configuration.authorizeEndpoint,
            configuration.tokenInfoEndpoint,
            configuration.tokenInfoCallback
    )
}