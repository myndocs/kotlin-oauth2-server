package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
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

private val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"

/**
 * @throws InvalidIdentityException
 * @throws InvalidClientException
 * @throws InvalidScopeException
 */
fun GrantingCall.authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse {
    throwExceptionIfUnverifiedClient(passwordGrantRequest)

    if (passwordGrantRequest.username == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
    }

    if (passwordGrantRequest.password == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
    }

    val requestedClient = clientService.clientOf(passwordGrantRequest.clientId!!) ?: throw InvalidClientException()

    val authorizedGrantType = AuthorizedGrantType.PASSWORD
    if (!requestedClient.authorizedGrantTypes.contains(authorizedGrantType)) {
        throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
    }

    val requestedIdentity = identityService.identityOf(
            requestedClient, passwordGrantRequest.username
    )

    if (requestedIdentity == null || !identityService.validCredentials(requestedClient, requestedIdentity, passwordGrantRequest.password)) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
            .toSet()

    if (passwordGrantRequest.scope == null) {
        requestedScopes = requestedClient.clientScopes
    }

    validateScopes(requestedClient, requestedIdentity, requestedScopes)

    val accessToken = converters.accessTokenConverter.convertToToken(
            requestedIdentity.username,
            requestedClient.clientId,
            requestedScopes,
            converters.refreshTokenConverter.convertToToken(
                    requestedIdentity.username,
                    requestedClient.clientId,
                    requestedScopes
            )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken.toTokenResponse()
}

fun GrantingCall.authorize(authorizationCodeRequest: AuthorizationCodeRequest): TokenResponse {
    throwExceptionIfUnverifiedClient(authorizationCodeRequest)

    if (authorizationCodeRequest.code == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("code"))
    }

    if (authorizationCodeRequest.redirectUri == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
    }

    val consumeCodeToken = tokenStore.consumeCodeToken(authorizationCodeRequest.code)
            ?: throw InvalidGrantException()


    if (consumeCodeToken.redirectUri != authorizationCodeRequest.redirectUri || consumeCodeToken.clientId != authorizationCodeRequest.clientId) {
        throw InvalidGrantException()
    }

    val accessToken = converters.accessTokenConverter.convertToToken(
            consumeCodeToken.username,
            consumeCodeToken.clientId,
            consumeCodeToken.scopes,
            converters.refreshTokenConverter.convertToToken(
                    consumeCodeToken.username,
                    consumeCodeToken.clientId,
                    consumeCodeToken.scopes
            )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken.toTokenResponse()
}

fun GrantingCall.authorize(clientCredentialsRequest: ClientCredentialsRequest): TokenResponse {
    throwExceptionIfUnverifiedClient(clientCredentialsRequest)

    val requestedClient = clientService.clientOf(clientCredentialsRequest.clientId!!) ?: throw InvalidClientException()

    val scopes = clientCredentialsRequest.scope
            ?.let { ScopeParser.parseScopes(it).toSet() }
            ?: requestedClient.clientScopes

    val accessToken = converters.accessTokenConverter.convertToToken(
            username = null,
            clientId = clientCredentialsRequest.clientId,
            requestedScopes = scopes,
            refreshToken = converters.refreshTokenConverter.convertToToken(
                    username = null,
                    clientId = clientCredentialsRequest.clientId,
                    requestedScopes = scopes
            )
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken.toTokenResponse()
}

fun GrantingCall.refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse {
    throwExceptionIfUnverifiedClient(refreshTokenRequest)

    if (refreshTokenRequest.refreshToken == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("refresh_token"))
    }

    val refreshToken = tokenStore.refreshToken(refreshTokenRequest.refreshToken) ?: throw InvalidGrantException()

    if (refreshToken.clientId != refreshTokenRequest.clientId) {
        throw InvalidGrantException()
    }

    val client = clientService.clientOf(refreshToken.clientId) ?: throw InvalidClientException()

    val authorizedGrantType = AuthorizedGrantType.REFRESH_TOKEN
    if (!client.authorizedGrantTypes.contains(authorizedGrantType)) {
        throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
    }

    val accessToken = converters.accessTokenConverter.convertToToken(
            refreshToken.username,
            refreshToken.clientId,
            refreshToken.scopes,
            converters.refreshTokenConverter.convertToToken(refreshToken)
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken.toTokenResponse()
}

fun GrantingCall.redirect(
        redirect: RedirectAuthorizationCodeRequest,
        authenticator: Authenticator?,
        identityScopeVerifier: IdentityScopeVerifier?
): CodeToken {
    if (redirect.clientId == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))
    }

    if (redirect.username == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
    }

    if (redirect.password == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
    }
    if (redirect.redirectUri == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
    }

    val clientOf = clientService.clientOf(redirect.clientId) ?: throw InvalidClientException()

    if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
        throw InvalidGrantException("invalid 'redirect_uri'")
    }

    val authorizedGrantType = AuthorizedGrantType.AUTHORIZATION_CODE
    if (!clientOf.authorizedGrantTypes.contains(authorizedGrantType)) {
        throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
    }

    val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

    var validIdentity = authenticator?.validCredentials(clientOf, identityOf, redirect.password)
            ?: identityService.validCredentials(clientOf, identityOf, redirect.password)

    if (!validIdentity) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(redirect.scope)

    if (redirect.scope == null) {
        requestedScopes = clientOf.clientScopes
    }

    validateScopes(clientOf, identityOf, requestedScopes, identityScopeVerifier)

    val codeToken = converters.codeTokenConverter.convertToToken(
            identityOf.username,
            clientOf.clientId,
            redirect.redirectUri,
            requestedScopes
    )

    tokenStore.storeCodeToken(codeToken)

    return codeToken
}

fun GrantingCall.redirect(
        redirect: RedirectTokenRequest,
        authenticator: Authenticator?,
        identityScopeVerifier: IdentityScopeVerifier?
): AccessToken {
    if (redirect.clientId == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))
    }

    if (redirect.username == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
    }

    if (redirect.password == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
    }
    if (redirect.redirectUri == null) {
        throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
    }

    val clientOf = clientService.clientOf(redirect.clientId) ?: throw InvalidClientException()

    if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
        throw InvalidGrantException("invalid 'redirect_uri'")
    }

    val authorizedGrantType = AuthorizedGrantType.IMPLICIT
    if (!clientOf.authorizedGrantTypes.contains(authorizedGrantType)) {
        throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
    }

    val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

    var validIdentity = authenticator?.validCredentials(clientOf, identityOf, redirect.password)
            ?: identityService.validCredentials(clientOf, identityOf, redirect.password)

    if (!validIdentity) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(redirect.scope)

    if (redirect.scope == null) {
        requestedScopes = clientOf.clientScopes
    }

    validateScopes(clientOf, identityOf, requestedScopes, identityScopeVerifier)

    val accessToken = converters.accessTokenConverter.convertToToken(
            identityOf.username,
            clientOf.clientId,
            requestedScopes,
            null
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}

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

private fun scopesAllowed(clientScopes: Set<String>, requestedScopes: Set<String>): Boolean {
    return clientScopes.containsAll(requestedScopes)
}

private fun AccessToken.toTokenResponse() = TokenResponse(
        accessToken,
        tokenType,
        expiresIn(),
        refreshToken?.refreshToken
)