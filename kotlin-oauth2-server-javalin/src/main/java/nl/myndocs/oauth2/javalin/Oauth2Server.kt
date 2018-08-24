package nl.myndocs.oauth2.javalin

import io.javalin.Context
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.exception.toMap
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.javalin.routing.*
import nl.myndocs.oauth2.javalin.util.BasicAuthorizer
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
        var authorizer: Authorizer<Context> = BasicAuthorizer
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

    this.routes {
        path(configuration.tokenEndpoint) {
            post { ctx ->
                try {
                    val allowedGrantTypes = setOf("password", "authorization_code", "refresh_token")
                    val grantType = ctx.formParam("grant_type")
                            ?: throw InvalidRequestException("'grant_type' not given")

                    if (!allowedGrantTypes.contains(grantType)) {
                        throw InvalidGrantException("'grant_type' with value '$grantType' not allowed")
                    }

                    val paramMap = ctx.formParamMap()
                            .mapValues { ctx.formParam(it.key) }

                    when (grantType) {
                        "password" -> routePasswordGrant(ctx, tokenService, paramMap)
                        "authorization_code" -> routeAuthorizationCodeGrant(ctx, tokenService, paramMap)
                        "refresh_token" -> routeRefreshTokenGrant(ctx, tokenService, paramMap)
                    }
                } catch (oauthException: OauthException) {
                    ctx.status(400)
                    ctx.json(oauthException.toMap())
                }

            }
        }

        path(configuration.authorizeEndpoint) {
            get { ctx ->
                try {
                    val allowedResponseTypes = setOf("code", "token")
                    val responseType = ctx.queryParam("response_type")
                            ?: throw InvalidRequestException("'response_type' not given")

                    if (!allowedResponseTypes.contains(responseType)) {
                        throw InvalidGrantException("'grant_type' with value '$responseType' not allowed")
                    }

                    val paramMap = ctx.queryParamMap()
                            .mapValues { ctx.queryParam(it.key) }

                    when (responseType) {
                        "code" -> routeAuthorizationCodeRedirect(ctx, tokenService, paramMap, configuration.authorizer)
                        "token" -> routeAccessTokenRedirect(ctx, tokenService, paramMap, configuration.authorizer)
                    }
                } catch (oauthException: OauthException) {
                    ctx.status(400)
                    ctx.json(oauthException.toMap())
                }
            }
        }

        path(configuration.userInfoEndpoint) {
            get { ctx ->
                val authorization = ctx.header("Authorization")

                if (authorization == null) {
                    ctx.status(401)
                    return@get
                }

                if (!authorization.startsWith("bearer ", true)) {
                    ctx.status(401)
                    return@get
                }

                val token = authorization.substring(7)

                val userInfoCallback = configuration.userInfoCallback(tokenService.userInfo(token))

                ctx.json(userInfoCallback)
            }
        }
    }
}
