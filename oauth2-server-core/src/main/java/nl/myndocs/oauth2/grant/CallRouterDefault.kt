package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.CodeChallengeMethod
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.token.CodeToken

fun GrantingCall.grantPassword() = granter("password") {
    val accessToken = authorize(
        PasswordGrantRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["username"],
            callContext.formParameters["password"],
            callContext.formParameters["scope"]
        )
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantClientCredentials() = granter("client_credentials") {
    val accessToken = authorize(
        ClientCredentialsRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["scope"]
        )
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantRefreshToken() = granter("refresh_token") {
    val accessToken = refresh(
        RefreshTokenRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["refresh_token"]
        )
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

fun GrantingCall.grantAuthorizationCode() = granter("authorization_code") {
    val accessToken = authorize(
        AuthorizationCodeRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["code"],
            callContext.formParameters["redirect_uri"],
            callContext.formParameters["code_verifier"]
        )
    )

    callContext.respondHeader("Cache-Control", "no-store")
    callContext.respondHeader("Pragma", "no-cache")
    callContext.respondJson(accessTokenResponder.createResponse(accessToken))
}

internal val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"

fun GrantingCall.validateScopes(
    client: Client,
    identity: Identity,
    requestedScopes: Set<String>
) {
    val scopesAllowed = scopesAllowed(client.clientScopes, requestedScopes)
    if (!scopesAllowed) {
        throw InvalidScopeException(requestedScopes.minus(client.clientScopes))
    }

    val allowedScopes = identityService.allowedScopes(client, identity, requestedScopes)

    val ivalidScopes = requestedScopes.minus(allowedScopes)
    if (ivalidScopes.isNotEmpty()) {
        throw InvalidScopeException(ivalidScopes)
    }
}

fun GrantingCall.tokenInfo(accessToken: String): TokenInfo {
    val storedAccessToken = tokenStore.accessToken(accessToken) ?: throw InvalidGrantException()
    val client = clientService.clientOf(storedAccessToken.clientId) ?: throw InvalidClientException()
    val identity = storedAccessToken.identity?.let { identityService.identityOf(client, it.username) }

    return TokenInfo(
        identity,
        client,
        storedAccessToken.scopes
    )
}

fun GrantingCall.throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
    val clientId = clientRequest.clientId
        ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))

    val client = clientService.clientOf(clientId) ?: throw InvalidClientException()
    if (client.public) {
        return
    }

    val clientSecret = clientRequest.clientSecret
        ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_secret"))

    if (!clientService.validClient(client, clientSecret)) {
        throw InvalidClientException()
    }
}

fun GrantingCall.scopesAllowed(clientScopes: Set<String>, requestedScopes: Set<String>): Boolean {
    return clientScopes.containsAll(requestedScopes)
}

fun GrantingCall.validateCodeChallenge(codeToken: CodeToken, request: AuthorizationCodeRequest) {
    val codeChallenge = codeToken.codeChallenge
    val codeVerifier = request.codeVerifier
    if (codeChallenge.isNullOrBlank() && request.codeVerifier.isNullOrBlank()) {
        return
    }

    if (codeChallenge.isNullOrBlank()) {
        throw InvalidGrantException()
    }

    if (codeVerifier.isNullOrBlank()) {
        throw InvalidGrantException()
    }

    val codeChallengeMethod = codeToken.codeChallengeMethod ?: CodeChallengeMethod.Plain
    val validChallengeCode = codeChallengeMethod.validate(codeChallenge = codeChallenge, codeVerifier = codeVerifier)
    if (!validChallengeCode) {
        throw InvalidGrantException()
    }
}
