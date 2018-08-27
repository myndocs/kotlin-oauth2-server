package nl.myndocs.oauth2.javalin

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.javalin.request.JavalinCallContext
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

data class OauthConfiguration(
        var identityService: IdentityService? = null,
        var clientService: ClientService? = null,
        var tokenStore: TokenStore? = null,
        var tokenEndpoint: String = "/oauth/token",
        var authorizeEndpoint: String = "/oauth/authorize",
        var userInfoEndpoint: String = "/oauth/userinfo",
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter(),
        var refreshTokenConverter: RefreshTokenConverter = UUIDRefreshTokenConverter(),
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter(),
        var userInfoCallback: (UserInfo) -> Map<String, Any?> = { userInfo ->
            mapOf(
                    "username" to userInfo.identity.username,
                    "scopes" to userInfo.scopes
            )
        },
        var authorizerFactory: (CallContext) -> Authorizer = ::BasicAuthorizer
)

fun Javalin.enableOauthServer(configurationCallback: OauthConfiguration.() -> Unit) {
    val configuration = OauthConfiguration()
    configuration.configurationCallback()

    val tokenService = TokenService(
            configuration.identityService!!,
            configuration.clientService!!,
            configuration.tokenStore!!,
            configuration.accessTokenConverter,
            configuration.refreshTokenConverter,
            configuration.codeTokenConverter
    )

    val callRouter = CallRouter(
            tokenService,
            configuration.tokenEndpoint,
            configuration.authorizeEndpoint,
            configuration.userInfoEndpoint,
            configuration.userInfoCallback
    )

    this.routes {
        path(configuration.tokenEndpoint) {
            post { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }

        path(configuration.authorizeEndpoint) {
            get { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }

        path(configuration.userInfoEndpoint) {
            get { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }
    }
}
