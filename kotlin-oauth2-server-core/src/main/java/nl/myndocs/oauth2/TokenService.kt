package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.InvalidScopeException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter

class TokenService(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore,
        private val accessTokenConverter: AccessTokenConverter,
        private val codeTokenConverter: CodeTokenConverter
) {
    /**
     * @throws InvalidIdentityException
     * @throws InvalidClientException
     * @throws InvalidScopeException
     */
    fun authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(passwordGrantRequest)

        val requestedClient = clientService.clientOf(
                passwordGrantRequest.clientId
        )!!
        val requestedIdentity = identityService.identityOf(
                requestedClient, passwordGrantRequest.username
        )

        if (requestedIdentity == null || !identityService.validIdentity(requestedClient, requestedIdentity, passwordGrantRequest.password)) {
            throw InvalidIdentityException()
        }

        var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
                .toSet()

        if (requestedScopes.isEmpty()) {
            requestedScopes = requestedClient.clientScopes
        }

        val clientDiffScopes = diffScopes(requestedClient.clientScopes, requestedScopes)

        if (clientDiffScopes.isNotEmpty()) {
            throw InvalidScopeException(clientDiffScopes)
        }

        val accessToken = accessTokenConverter.convertToToken(
                requestedIdentity.username,
                requestedClient.clientId,
                requestedScopes
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    fun authorize(authorizationCodeRequest: AuthorizationCodeRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(authorizationCodeRequest)

        val consumeCodeToken = tokenStore.consumeCodeToken(authorizationCodeRequest.code)
                ?: throw InvalidGrantException()


        if (consumeCodeToken.redirectUri != authorizationCodeRequest.redirectUri || consumeCodeToken.clientId != authorizationCodeRequest.clientId) {
            throw InvalidGrantException()
        }

        val accessToken = accessTokenConverter.convertToToken(
                consumeCodeToken.username,
                consumeCodeToken.clientId,
                consumeCodeToken.scopes
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    fun refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(refreshTokenRequest)

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
        val clientOf = clientService.clientOf(redirect.clientId) ?: throw InvalidClientException()
        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

        var validIdentity = identityService.validIdentity(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw InvalidIdentityException()
        }

        val requestedScopes = ScopeParser.parseScopes(redirect.scope)

        val diffScopes = diffScopes(clientOf.clientScopes, requestedScopes)
        if (diffScopes.isNotEmpty()) {
            throw InvalidScopeException(diffScopes)
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

    private fun throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
        val client = clientService.clientOf(clientRequest.clientId) ?: throw InvalidClientException()

        if (!clientService.validClient(client, clientRequest.clientSecret)) {
            throw InvalidClientException()
        }
    }

    private fun diffScopes(allowedScopes: Set<String>, validationScopes: Set<String>): Set<String> {
        if (allowedScopes.containsAll(validationScopes)) {
            return validationScopes.minus(allowedScopes)
        }

        return setOf()
    }

    private fun AccessToken.toTokenResponse() = TokenResponse(
            accessToken,
            tokenType,
            expiresIn(),
            refreshToken?.refreshToken
    )
}