package nl.myndocs.oauth2.authenticator

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity

interface IdentityScopeVerifier {
    fun validScopes(forClient: Client, identity: Identity, scopes: Set<String>): Boolean
}