package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.client.UnverifiedClientException
import nl.myndocs.oauth2.code.InvalidAuthorizationCode
import nl.myndocs.oauth2.code.UnverifiedAuthorizationCode
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UnverifiedIdentity
import nl.myndocs.oauth2.request.AuthorizationCodeRequest
import nl.myndocs.oauth2.request.ClientRequest
import nl.myndocs.oauth2.request.PasswordGrantRequest
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.RequestedScopeNotAllowed
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter

class TokenService(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore,
        private val accessTokenConverter: AccessTokenConverter
) {
    /**
     * @throws UnverifiedIdentity
     * @throws UnverifiedClientException
     * @throws RequestedScopeNotAllowed
     */
    fun authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(passwordGrantRequest)

        val requestedIdentity = identityService.identityOf(
                passwordGrantRequest.username
        )

        if (requestedIdentity == null || !identityService.validIdentity(requestedIdentity, passwordGrantRequest.password)) {
            throw UnverifiedIdentity()
        }

        var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
                .toSet()

        val requestedClient = clientService.clientOf(
                passwordGrantRequest.clientId
        )!!

        if (requestedScopes.isEmpty()) {
            requestedScopes = requestedClient.clientScopes
        }

        val clientDiffScopes = diffScopes(requestedClient.clientScopes, requestedScopes)
                .plus(diffScopes(requestedIdentity.allowedScopes, requestedScopes))

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

    private fun throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
        val client = clientService.clientOf(clientRequest.clientId)

        if (!clientService.validClient(client!!, clientRequest.clientSecret)) {
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
            expiresIn,
            refreshToken?.refreshToken
    )
}