package nl.myndocs.oauth2.javalin.routing

import io.javalin.Context
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.ktor.feature.util.BasicAuth
import nl.myndocs.oauth2.request.RedirectAuthorizationCodeRequest
import nl.myndocs.oauth2.request.RedirectTokenRequest


fun routeAuthorizationCodeRedirect(ctx: Context, tokenService: TokenService, queryParameters: Map<String, String?>) {
    val authorizationHeader = ctx.header("authorization") ?: ""
    val credentials = BasicAuth.parse(authorizationHeader)

    try {
        val redirect = tokenService.redirect(
                RedirectAuthorizationCodeRequest(
                        queryParameters["client_id"],
                        queryParameters["redirect_uri"],
                        credentials.username ?: "",
                        credentials.password ?: "",
                        queryParameters["scope"]
                )
        )

        var stateQueryParameter = ""

        if (queryParameters["state"] != null) {
            stateQueryParameter = "&state=" + queryParameters["state"]
        }

        ctx.redirect(queryParameters["redirect_uri"] + "?code=${redirect.codeToken}$stateQueryParameter")
    } catch (unverifiedIdentityException: InvalidIdentityException) {
        ctx.header("WWW-Authenticate", "Basic realm=\"${queryParameters["client_id"]}\"")
        ctx.status(401)
    }
}


fun routeAccessTokenRedirect(ctx: Context, tokenService: TokenService, queryParameters: Map<String, String?>) {
    val authorizationHeader = ctx.header("authorization") ?: ""
    val credentials = BasicAuth.parse(authorizationHeader)

    try {
        val redirect = tokenService.redirect(
                RedirectTokenRequest(
                        queryParameters["client_id"],
                        queryParameters["redirect_uri"],
                        credentials.username ?: "",
                        credentials.password ?: "",
                        queryParameters["scope"]
                )
        )

        var stateQueryParameter = ""

        if (queryParameters["state"] != null) {
            stateQueryParameter = "&state=" + queryParameters["state"]
        }

        ctx.redirect(
                queryParameters["redirect_uri"] + "#access_token=${redirect.accessToken}" +
                        "&token_type=bearer&expires_in=${redirect.expiresIn()}$stateQueryParameter"
        )

    } catch (unverifiedIdentityException: InvalidIdentityException) {
        ctx.header("WWW-Authenticate", "Basic realm=\"${queryParameters["client_id"]}\"")
        ctx.status(401)
    }
}