package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.ktor.feature.json.MapToJson
import nl.myndocs.oauth2.ktor.feature.routing.authorize.configureAuthorizeEndpoint
import nl.myndocs.oauth2.ktor.feature.routing.token.configureTokenEndpoint
import nl.myndocs.oauth2.ktor.feature.util.BasicAuthorizer
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
    val authorizer: Authorizer<ApplicationCall> = configuration.authorizer

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
        var authorizer: Authorizer<ApplicationCall> = BasicAuthorizer()
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Oauth2ServerFeature.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Oauth2ServerFeature {

            val configuration = Oauth2ServerFeature.Configuration().apply(configure)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Infrastructure) {
                configureTokenEndpoint(feature)
                configureAuthorizeEndpoint(feature)

                if (call.request.httpMethod != HttpMethod.Get) {
                    proceed()
                    return@intercept
                }

                val requestPath = call.request.path()
                if (requestPath != feature.userInfoEndpoint) {
                    proceed()
                    return@intercept
                }

                val authorization = call.request.header("Authorization")

                if (authorization == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    finish()
                    return@intercept
                }

                if (!authorization.startsWith("bearer ", true)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    finish()
                    return@intercept
                }

                val token = authorization.substring(7)

                val userInfoCallback = feature.userInfoCallback(feature.tokenService.userInfo(token))

                call.respond(MapToJson.toJson(userInfoCallback))
                finish()
                return@intercept
            }

            return feature
        }
    }
}
