package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.toMap

fun GrantingCall.grantPassword() = granter("password") {
    val tokenResponse = authorize(
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
    val tokenResponse = authorize(ClientCredentialsRequest(
            callContext.formParameters["client_id"],
            callContext.formParameters["client_secret"],
            callContext.formParameters["scope"]
    ))

    callContext.respondJson(tokenResponse.toMap())
}

fun GrantingCall.grantRefreshToken() = granter("refresh_token") {
    val accessToken = refresh(
            RefreshTokenRequest(
                    callContext.formParameters["client_id"],
                    callContext.formParameters["client_secret"],
                    callContext.formParameters["refresh_token"]
            )
    )

    callContext.respondJson(accessToken.toMap())
}

fun GrantingCall.grantAuthorizationCode() = granter("authorization_code") {
    val accessToken = authorize(
            AuthorizationCodeRequest(
                    callContext.formParameters["client_id"],
                    callContext.formParameters["client_secret"],
                    callContext.formParameters["code"],
                    callContext.formParameters["redirect_uri"]
            )
    )

    callContext.respondJson(accessToken.toMap())
}

internal val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"

fun GrantingCall.validateScopes(
        client: Client,
        identity: Identity,
        requestedScopes: Set<String>,
        identityScopeVerifier: IdentityScopeVerifier? = null) {
    val scopesAllowed = scopesAllowed(client.clientScopes, requestedScopes)
    if (!scopesAllowed) {
        throw InvalidScopeException(requestedScopes.minus(client.clientScopes))
    }

    val allowedScopes = identityScopeVerifier?.allowedScopes(client, identity, requestedScopes)
            ?: identityService.allowedScopes(client, identity, requestedScopes)

    val ivalidScopes = requestedScopes.minus(allowedScopes)
    if (ivalidScopes.isNotEmpty()) {
        throw InvalidScopeException(ivalidScopes)
    }
}

fun GrantingCall.tokenInfo(accessToken: String): TokenInfo {
    val storedAccessToken = tokenStore.accessToken(accessToken) ?: throw InvalidGrantException()
    val client = clientService.clientOf(storedAccessToken.clientId) ?: throw InvalidClientException()
    val identity = storedAccessToken.username?.let { identityService.identityOf(client, it) }

    return TokenInfo(
            identity,
            client,
            storedAccessToken.scopes
    )
}

fun GrantingCall.throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
    val clientId = clientRequest.clientId
            ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))

    val clientSecret = clientRequest.clientSecret
            ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_secret"))

    val client = clientService.clientOf(clientId) ?: throw InvalidClientException()

    if (!clientService.validClient(client, clientSecret)) {
        throw InvalidClientException()
    }
}

fun GrantingCall.scopesAllowed(clientScopes: Set<String>, requestedScopes: Set<String>): Boolean {
    return clientScopes.containsAll(requestedScopes)
}

fun AccessToken.toTokenResponse() = TokenResponse(
        accessToken,
        tokenType,
        expiresIn(),
        refreshToken?.refreshToken
)