package nl.myndocs.oauth2.authenticator

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity

interface IdentityScopeVerifier {
    /**
     * Validate which scopes are allowed. Leave out the scopes which are not allowed
     */
    fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String>
}