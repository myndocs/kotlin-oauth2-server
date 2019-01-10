package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.token.toMap

fun GrantingCall.grantPassword() = granter("password") {
    val tokenResponse = tokenService.authorize(
            PasswordGrantRequest(
                    callContext.formParameters["client_id"],
                    callContext.formParameters["client_secret"],
                    callContext.formParameters["username"],
                    callContext.formParameters["password"],
                    callContext.formParameters["scope"]
            )
    )

    callContext.respondJson(tokenResponse.toMap())
}

fun GrantingCall.grantClientCredentials() = granter("client_credentials") {
    val tokenResponse = tokenService.authorize(ClientCredentialsRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["scope"]
    ))

    callContext.respondJson(tokenResponse.toMap())
}

fun GrantingCall.grantRefreshToken() = granter("refresh_token") {
    val accessToken = tokenService.refresh(
            RefreshTokenRequest(
                    callContext.formParameters["client_id"],
                    callContext.formParameters["client_secret"],
                    callContext.formParameters["refresh_token"]
            )
    )

    callContext.respondJson(accessToken.toMap())
}

fun GrantingCall.grantAuthorizationCode() = granter("authorization_code") {
    val accessToken = tokenService.authorize(
            AuthorizationCodeRequest(
                    callContext.formParameters["client_id"],
                    callContext.formParameters["client_secret"],
                    callContext.formParameters["code"],
                    callContext.formParameters["redirect_uri"]
            )
    )

    callContext.respondJson(accessToken.toMap())
}
