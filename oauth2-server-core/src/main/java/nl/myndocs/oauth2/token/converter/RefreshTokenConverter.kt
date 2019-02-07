package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.RefreshToken

interface RefreshTokenConverter {
    fun convertToToken(refreshToken: RefreshToken): RefreshToken = convertToToken(refreshToken.identity, refreshToken.clientId, refreshToken.scopes)

    fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>): RefreshToken
}