package nl.myndocs.oauth2.grant

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

fun GrantingCall.redirect(redirect: RedirectAuthorizationCodeRequest): CodeToken {
    checkMissingFields(redirect)

    val clientOf = clientService.clientOf(redirect.clientId!!) ?: throw InvalidClientException()
    if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
        throw InvalidGrantException("invalid 'redirect_uri'")
    }

    with(AuthorizedGrantType.AUTHORIZATION_CODE) {
        if (!clientOf.authorizedGrantTypes.contains(this)) {
            throw InvalidGrantException("Authorize not allowed: '$this'")
        }
    }

    val identityOf = identityService.identityOf(clientOf, redirect.username!!) ?: throw InvalidIdentityException()

    val validIdentity = identityService.validCredentials(clientOf, identityOf, redirect.password!!)
    if (!validIdentity) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(redirect.scope)
    if (redirect.scope == null) {
        requestedScopes = clientOf.clientScopes
    }

    validateScopes(clientOf, identityOf, requestedScopes)

    val codeToken = converters.codeTokenConverter.convertToToken(
        identityOf,
        clientOf.clientId,
        redirect.redirectUri!!,
        requestedScopes
    )

    tokenStore.storeCodeToken(codeToken)

    return codeToken
}

fun GrantingCall.redirect(redirect: RedirectTokenRequest): AccessToken {
    checkMissingFields(redirect)

    val clientOf = clientService.clientOf(redirect.clientId!!) ?: throw InvalidClientException()
    if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
        throw InvalidGrantException("invalid 'redirect_uri'")
    }

    with(AuthorizedGrantType.IMPLICIT) {
        if (!clientOf.authorizedGrantTypes.contains(this)) {
            throw InvalidGrantException("Authorize not allowed: '$this'")
        }
    }

    val identityOf = identityService.identityOf(clientOf, redirect.username!!) ?: throw InvalidIdentityException()

    val validIdentity = identityService.validCredentials(clientOf, identityOf, redirect.password!!)
    if (!validIdentity) {
        throw InvalidIdentityException()
    }

    var requestedScopes = ScopeParser.parseScopes(redirect.scope)
    if (redirect.scope == null) {
        // @TODO: This behavior is not in the spec and should be configurable https://tools.ietf.org/html/rfc6749#section-3.3
        requestedScopes = clientOf.clientScopes
    }

    validateScopes(clientOf, identityOf, requestedScopes)

    val accessToken = converters.accessTokenConverter.convertToToken(
        identityOf,
        clientOf.clientId,
        requestedScopes,
        null
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken
}

private fun throwMissingField(field: String): Nothing =
    throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format(field))

private fun checkMissingFields(redirect: RedirectTokenRequest) = with(redirect) {
    when {
        clientId == null -> throwMissingField("client_id")
        username == null -> throwMissingField("username")
        password == null -> throwMissingField("password")
        redirectUri == null -> throwMissingField("redirect_uri")
        else -> this
    }
}

private fun checkMissingFields(redirect: RedirectAuthorizationCodeRequest) = with(redirect) {
    when {
        clientId == null -> throwMissingField("client_id")
        username == null -> throwMissingField("username")
        password == null -> throwMissingField("password")
        redirectUri == null -> throwMissingField("redirect_uri")
        else -> this
    }
}