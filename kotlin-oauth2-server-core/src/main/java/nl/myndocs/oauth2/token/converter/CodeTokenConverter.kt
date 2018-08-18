package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.CodeToken

interface CodeTokenConverter {
    fun convertToToken(username: String, clientId: String, redirectUri: String, requestedScopes: Set<String>): CodeToken
}