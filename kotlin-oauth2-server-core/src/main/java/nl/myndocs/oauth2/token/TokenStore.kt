package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity

interface TokenStore {
    fun generateAndStoreTokenFor(identity: Identity, client: Client): Token
}