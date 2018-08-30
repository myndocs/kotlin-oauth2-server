package nl.myndocs.oauth2.identity

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.Client

interface IdentityService : Authenticator, IdentityScopeVerifier {
    /**
     * Find identity within a client and username
     * If not found return null
     */
    fun identityOf(forClient: Client, username: String): Identity?
}