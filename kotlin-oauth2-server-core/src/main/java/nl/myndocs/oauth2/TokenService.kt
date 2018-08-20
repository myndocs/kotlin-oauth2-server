package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter

class TokenService(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore,
        private val accessTokenConverter: AccessTokenConverter,
        private val refreshTokenConverter: RefreshTokenConverter,
        private val codeTokenConverter: CodeTokenConverter
) {
    private val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"
    /**
     * @throws InvalidIdentityException
     * @throws InvalidClientException
     * @throws InvalidScopeException
     */
    fun authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(passwordGrantRequest)

        if (passwordGrantRequest.username == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
        }

        if (passwordGrantRequest.password == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
        }

        val requestedClient = clientService.clientOf(
                passwordGrantRequest.clientId!!
        )!!
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

        val scopesAllowed = scopesAllowed(requestedClient.clientScopes, requestedScopes)

        if (!scopesAllowed) {
            throw InvalidScopeException(requestedScopes.minus(requestedClient.clientScopes))
        }

        if (!identityService.validScopes(requestedClient, requestedIdentity, requestedScopes)) {
            throw InvalidScopeException(requestedScopes)
        }

        val accessToken = accessTokenConverter.convertToToken(
                requestedIdentity.username,
                requestedClient.clientId,
                requestedScopes,
                refreshTokenConverter.convertToToken(
                        requestedIdentity.username,
                        requestedClient.clientId,
                        requestedScopes
                )
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    fun authorize(authorizationCodeRequest: AuthorizationCodeRequest): TokenResponse {
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

        val accessToken = accessTokenConverter.convertToToken(
                consumeCodeToken.username,
                consumeCodeToken.clientId,
                consumeCodeToken.scopes,
                refreshTokenConverter.convertToToken(
                        consumeCodeToken.username,
                        consumeCodeToken.clientId,
                        consumeCodeToken.scopes
                )
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    fun refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(refreshTokenRequest)

        if (refreshTokenRequest.refreshToken == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("refresh_token"))
        }

        val refreshToken = tokenStore.refreshToken(refreshTokenRequest.refreshToken) ?: throw InvalidGrantException()

        val accessToken = accessTokenConverter.convertToToken(
                refreshToken.username,
                refreshToken.clientId,
                refreshToken.scopes,
                refreshToken
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    fun redirect(redirect: RedirectAuthorizationCodeRequest): CodeToken {
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

        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

        var validIdentity = identityService.validCredentials(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw InvalidIdentityException()
        }

        val requestedScopes = ScopeParser.parseScopes(redirect.scope)

        val scopesAllowed = scopesAllowed(clientOf.clientScopes, requestedScopes)
        if (!scopesAllowed) {
            throw InvalidScopeException(requestedScopes.minus(clientOf.clientScopes))
        }

        val codeToken = codeTokenConverter.convertToToken(
                identityOf.username,
                clientOf.clientId,
                redirect.redirectUri,
                requestedScopes
        )

        tokenStore.storeCodeToken(codeToken)

        return codeToken
    }

    fun redirect(redirect: RedirectTokenRequest): AccessToken {
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

        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

        var validIdentity = identityService.validCredentials(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw InvalidIdentityException()
        }

        val requestedScopes = ScopeParser.parseScopes(redirect.scope)

        val scopesAllowed = scopesAllowed(clientOf.clientScopes, requestedScopes)
        if (!scopesAllowed) {
            throw InvalidScopeException(requestedScopes.minus(clientOf.clientScopes))
        }

        val accessToken = accessTokenConverter.convertToToken(
                identityOf.username,
                clientOf.clientId,
                requestedScopes,
                null
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken
    }

    private fun throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
        if (clientRequest.clientId == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))
        }

        if (clientRequest.clientSecret == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_secret"))
        }

        val client = clientService.clientOf(clientRequest.clientId!!) ?: throw InvalidClientException()

        if (!clientService.validClient(client, clientRequest.clientSecret!!)) {
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
}