package nl.myndocs.oauth2.javalin.routing

import io.javalin.Context
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.request.RefreshTokenRequest
import nl.myndocs.oauth2.token.toMap


fun routePasswordGrant(ctx: Context, tokenService: TokenService, formParams: Map<String, String?>) {
    val tokenResponse = tokenService.authorize(
            PasswordGrantRequest(
                    formParams["client_id"],
                    formParams["client_secret"],
                    formParams["username"],
                    formParams["password"],
                    formParams["scope"]
            )
    )

    ctx.json(tokenResponse.toMap())
}

fun routeRefreshTokenGrant(ctx: Context, tokenService: TokenService, formParams: Map<String, String?>) {
    val accessToken = tokenService.refresh(
            RefreshTokenRequest(
                    formParams["client_id"],
                    formParams["client_secret"],
                    formParams["refresh_token"]
            )
    )

    ctx.json(accessToken.toMap())
}

fun routeAuthorizationCodeGrant(ctx: Context, tokenService: TokenService, formParams: Map<String, String?>) {
    val accessToken = tokenService.authorize(
            AuthorizationCodeRequest(
                    formParams["client_id"],
                    formParams["client_secret"],
                    formParams["code"],
                    formParams["redirect_uri"]
            )
    )

    ctx.json(accessToken.toMap())
}
