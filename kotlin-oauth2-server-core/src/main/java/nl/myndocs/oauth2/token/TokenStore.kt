package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity

interface TokenStore {
    fun generateAndStoreTokenFor(identity: Identity, client: Client, requestedScopes: Set<String>): Token

    fun generateCodeTokenAndStoreFor(identity: Identity, client: Client, redirectUri: String, requestedScopes: Set<String>): String
}