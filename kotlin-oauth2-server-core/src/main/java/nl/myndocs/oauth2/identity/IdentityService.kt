package nl.myndocs.oauth2.identity

import nl.myndocs.oauth2.client.Client

interface IdentityService {
    /**
     * Find identity within a client and username
     * If not found return null
     */
    fun identityOf(forClient: Client, username: String): Identity?

    fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean

    fun validScopes(forClient: Client, identity: Identity, scopes: Set<String>): Boolean
}