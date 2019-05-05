package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.request.RedirectAuthorizationCodeRequest
import nl.myndocs.oauth2.request.RedirectTokenRequest
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken


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
            identityOf,
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
            identityOf,
            clientOf.clientId,
            requestedScopes,
            null
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}
