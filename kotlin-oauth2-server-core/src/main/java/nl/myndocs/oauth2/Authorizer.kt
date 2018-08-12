package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.client.UnverifiedClientException
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UnverifiedIdentity
import nl.myndocs.oauth2.requeset.PasswordGrantRequest
import nl.myndocs.oauth2.response.PasswordGrantResponse
import nl.myndocs.oauth2.scope.RequestedScopeNotAllowed
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.TokenStore

class Authorizer(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore
) {
    /**
     * @throws UnverifiedIdentity
     * @throws UnverifiedClientException
     * @throws RequestedScopeNotAllowed
     */
    fun authorize(passwordGrantRequest: PasswordGrantRequest): PasswordGrantResponse {
        val requestedClient = clientService.clientOf(
                passwordGrantRequest.clientId
        )

        if (requestedClient == null || !requestedClient.secretVerifier(passwordGrantRequest.clientSecret)) {
            throw UnverifiedClientException()
        }


        val requestedIdentity = identityService.identityOF(
                passwordGrantRequest.username
        )

        if (requestedIdentity == null || !requestedIdentity.passwordVerifier(passwordGrantRequest.password)) {
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

        val identityDiffScopes = diffScopes(requestedClient.clientScopes, requestedScopes)
        if (identityDiffScopes.isNotEmpty()) {
            throw RequestedScopeNotAllowed(identityDiffScopes)
        }

        val token = tokenStore.generateAndStoreTokenFor(
                requestedIdentity,
                requestedClient
        )

        return PasswordGrantResponse(
                token.accessToken,
                token.tokenType,
                token.expiresIn,
                token.refreshToken
        )
    }

    private fun diffScopes(allowedScopes: Set<String>, validationScopes: Set<String>): Set<String> {
        if (allowedScopes.containsAll(validationScopes)) {
            return validationScopes.minus(allowedScopes)
        }

        return setOf()
    }
}