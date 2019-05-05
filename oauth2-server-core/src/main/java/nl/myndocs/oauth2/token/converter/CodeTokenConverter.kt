package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.CodeToken

interface CodeTokenConverter {
    fun convertToToken(identity: Identity, clientId: String, redirectUri: String, requestedScopes: Set<String>): CodeToken
}