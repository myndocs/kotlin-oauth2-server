package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.RefreshToken

interface RefreshTokenConverter {
    fun convertToToken(username: String?, clientId: String, requestedScopes: Set<String>): RefreshToken
}