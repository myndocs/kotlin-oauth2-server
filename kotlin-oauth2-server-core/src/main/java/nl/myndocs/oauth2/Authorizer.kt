package nl.myndocs.oauth2

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.requeset.PasswordGrantRequest
import nl.myndocs.oauth2.response.PasswordGrantResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.TokenStore

class Authorizer(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore
) {
    /**
     * @throws nl.myndocs.oauth2.identity.UnverifiedIdentity
     * @throws nl.myndocs.oauth2.client.UnverifiedClientException
     */
    fun authorize(passwordGrantRequest: PasswordGrantRequest): PasswordGrantResponse {
        val verifiedClient = clientService.verifiedClientOf(
                passwordGrantRequest.clientId,
                passwordGrantRequest.clientSecret
        )

        val verifiedIdentity = identityService.verifiedIdentityOf(
                passwordGrantRequest.username,
                passwordGrantRequest.password
        )

        val requestScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
        // @TODO: SCOPES check
        // @TODO: Default behavior when scopes are not set?


        val token = tokenStore.generateAndStoreTokenFor(
                verifiedIdentity,
                verifiedClient
        )

        return PasswordGrantResponse(
                token.accessToken,
                token.tokenType,
                token.expiresIn,
                token.refreshToken
        )
    }
}