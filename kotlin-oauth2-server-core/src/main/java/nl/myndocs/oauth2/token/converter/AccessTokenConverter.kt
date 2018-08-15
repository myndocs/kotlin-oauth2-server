package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken

interface AccessTokenConverter : TokenConverter<AccessToken> {
    fun convertToToken(username: String, clientId: String, requestedScopes: Set<String>, refreshToken: RefreshToken?): AccessToken
}