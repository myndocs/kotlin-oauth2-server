package nl.myndocs.oauth2.identity

import nl.myndocs.oauth2.client.Client

interface IdentityService {
    fun identityOf(forClient: Client, username: String): Identity?

    fun validIdentity(forClient: Client, identity: Identity, password: String): Boolean
}