package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer

object ConfigurationBuilder {
    class Configuration {
        internal val callRouterConfiguration = CallRouterBuilder.Configuration()

        var tokenService: TokenService?
            get() = callRouterConfiguration.tokenService
            set(value) {
                callRouterConfiguration.tokenService = value
            }

        var authorizationEndpoint: String
            get() = callRouterConfiguration.authorizeEndpoint
            set(value) {
                callRouterConfiguration.authorizeEndpoint = value
            }

        var tokenEndpoint: String
            get() = callRouterConfiguration.tokenEndpoint
            set(value) {
                callRouterConfiguration.tokenEndpoint = value
            }

        var userInfoEndpoint: String
            get() = callRouterConfiguration.userInfoEndpoint
            set(value) {
                callRouterConfiguration.userInfoEndpoint = value
            }

        var userInfoCallback: (UserInfo) -> Map<String, Any?>
            get() = callRouterConfiguration.userInfoCallback
            set(value) {
                callRouterConfiguration.userInfoCallback = value
            }

        var authorizerFactory: (CallContext) -> Authorizer = ::BasicAuthorizer
    }

    fun build(configurer: Configuration.() -> Unit): nl.myndocs.oauth2.config.Configuration {
        val configuration = Configuration()
        configurer(configuration)

        return nl.myndocs.oauth2.config.Configuration(
                configuration.tokenService!!,
                CallRouterBuilder.build(configuration.callRouterConfiguration),
                configuration.authorizerFactory
        )
    }
}