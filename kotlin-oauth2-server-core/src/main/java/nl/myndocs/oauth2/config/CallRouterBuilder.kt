package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.identity.UserInfo

internal object CallRouterBuilder {
    class Configuration {
        var tokenEndpoint: String = "/oauth/token"
        var authorizeEndpoint: String = "/oauth/authorize"
        var userInfoEndpoint: String = "/oauth/userinfo"
        var userInfoCallback: (UserInfo) -> Map<String, Any?> = { userInfo ->
            mapOf(
                    "username" to userInfo.identity.username,
                    "scopes" to userInfo.scopes
            )
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
            configuration.tokenEndpoint,
            configuration.authorizeEndpoint,
            configuration.userInfoEndpoint,
            configuration.userInfoCallback
    )
}