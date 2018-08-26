package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

class Oauth2ServerFeature(configuration: Configuration) {
    val tokenEndpoint = configuration.tokenEndpoint
    val authorizeEndpoint = configuration.authorizeEndpoint
    val clientService = configuration.clientService!!
    val identityService = configuration.identityService!!
    val tokenStore = configuration.tokenStore!!
    val accessTokenConverter = configuration.accessTokenConverter
    val refreshTokenConverter = configuration.refreshTokenConverter
    val codeTokenConverter = configuration.codeTokenConverter
    val userInfoEndpoint = configuration.userInfoEndpoint
    val userInfoCallback = configuration.userInfoCallback
    val tokenService = TokenService(
            identityService,
            clientService,
            tokenStore,
            accessTokenConverter,
            refreshTokenConverter,
            codeTokenConverter
    )
    val callRouter = CallRouter(
            tokenService,
            tokenEndpoint,
            authorizeEndpoint,
            userInfoEndpoint,
            userInfoCallback
    )
    val authorizerFactory: (CallContext<ApplicationCall>) -> Authorizer = configuration.authorizerFactory

    class Configuration {
        var tokenEndpoint = "/oauth/token"
        var authorizeEndpoint = "/oauth/authorize"
        var userInfoEndpoint = "/oauth/userinfo"
        var clientService: ClientService? = null
        var identityService: IdentityService? = null
        var tokenStore: TokenStore? = null
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter()
        val refreshTokenConverter = UUIDRefreshTokenConverter()
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter()
        var userInfoCallback: (UserInfo) -> Map<String, Any?> = { userInfo ->
            mapOf(
                    "username" to userInfo.identity.username,
                    "scopes" to userInfo.scopes
            )
        }
        var authorizerFactory: (CallContext<ApplicationCall>) -> Authorizer = ::BasicAuthorizer
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Oauth2ServerFeature.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Oauth2ServerFeature {

            val configuration = Oauth2ServerFeature.Configuration().apply(configure)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Infrastructure) {
                val ktorCallContext = KtorCallContext(call)
                val authorizer = feature.authorizerFactory(ktorCallContext)

                feature.callRouter.route(ktorCallContext, authorizer)
            }

            return feature
        }
    }
}
