package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.grant.Granter
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.response.AccessTokenResponder
import nl.myndocs.oauth2.response.DefaultAccessTokenResponder
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

object ConfigurationBuilder {
    class Configuration {
        internal val callRouterConfiguration = CallRouterBuilder.Configuration()

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

        var tokenInfoEndpoint: String
            get() = callRouterConfiguration.tokenInfoEndpoint
            set(value) {
                callRouterConfiguration.tokenInfoEndpoint = value
            }

        var tokenInfoCallback: (TokenInfo) -> Map<String, Any?>
            get() = callRouterConfiguration.tokenInfoCallback
            set(value) {
                callRouterConfiguration.tokenInfoCallback = value
            }

        var granters: List<GrantingCall.() -> Granter>
            get() = callRouterConfiguration.granters
            set(value) {
                callRouterConfiguration.granters = value
            }

        var authorizerFactory: (CallContext) -> Authorizer = ::BasicAuthorizer

        var identityService: IdentityService? = null
        var clientService: ClientService? = null
        var tokenStore: TokenStore? = null
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter()
        var refreshTokenConverter: RefreshTokenConverter = UUIDRefreshTokenConverter()
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter()
        var accessTokenResponder: AccessTokenResponder = DefaultAccessTokenResponder
    }

    fun build(configurer: Configuration.() -> Unit): nl.myndocs.oauth2.config.Configuration {
        val configuration = Configuration()
        configurer(configuration)

        val grantingCallFactory: (CallContext) -> GrantingCall = { callContext ->
            object : GrantingCall {
                override val callContext = callContext
                override val identityService = configuration.identityService!!
                override val clientService = configuration.clientService!!
                override val tokenStore = configuration.tokenStore!!
                override val converters = Converters(
                        configuration.accessTokenConverter,
                        configuration.refreshTokenConverter,
                        configuration.codeTokenConverter
                )
                override val accessTokenResponder = configuration.accessTokenResponder
            }
        }
        return Configuration(
                CallRouterBuilder.build(
                        configuration.callRouterConfiguration,
                        grantingCallFactory,
                        configuration.authorizerFactory
                )
        )
    }
}