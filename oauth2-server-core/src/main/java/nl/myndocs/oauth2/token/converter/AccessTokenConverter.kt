package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken

interface AccessTokenConverter {
    fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>, refreshToken: RefreshToken?): AccessToken
}