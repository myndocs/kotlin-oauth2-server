package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientDoesNotExist
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.client.UnverifiedClientException
import nl.myndocs.oauth2.code.InvalidAuthorizationCode
import nl.myndocs.oauth2.code.UnverifiedAuthorizationCode
import nl.myndocs.oauth2.identity.IdentityDoesNotExist
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UnverifiedIdentity
import nl.myndocs.oauth2.refresh.InvalidRefreshToken
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.RequestedScopeNotAllowed
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
     * @throws UnverifiedIdentity
     * @throws UnverifiedClientException
     * @throws RequestedScopeNotAllowed
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
            throw UnverifiedIdentity()
        }

        var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
                .toSet()

        if (requestedScopes.isEmpty()) {
            requestedScopes = requestedClient.clientScopes
        }

        val clientDiffScopes = diffScopes(requestedClient.clientScopes, requestedScopes)

        if (clientDiffScopes.isNotEmpty()) {
            throw RequestedScopeNotAllowed(clientDiffScopes)
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
                ?: throw InvalidAuthorizationCode()


        if (consumeCodeToken.redirectUri != authorizationCodeRequest.redirectUri || consumeCodeToken.clientId != authorizationCodeRequest.clientId) {
            throw UnverifiedAuthorizationCode()
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

        val refreshToken = tokenStore.refreshToken(refreshTokenRequest.refreshToken) ?: throw InvalidRefreshToken()

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
        val clientOf = clientService.clientOf(redirect.clientId) ?: throw ClientDoesNotExist()
        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw IdentityDoesNotExist()

        var validIdentity = identityService.validIdentity(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw UnverifiedIdentity()
        }

        val requestedScopes = ScopeParser.parseScopes(redirect.scope)

        val diffScopes = diffScopes(clientOf.clientScopes, requestedScopes)
        if (diffScopes.isNotEmpty()) {
            throw RequestedScopeNotAllowed(diffScopes)
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
        val client = clientService.clientOf(clientRequest.clientId) ?: throw ClientDoesNotExist()

        if (!clientService.validClient(client, clientRequest.clientSecret)) {
            throw UnverifiedClientException()
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