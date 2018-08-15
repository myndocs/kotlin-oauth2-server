package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant
import java.util.*

class UUIDAccessTokenConverter(
        private val accessTokenExpireInSeconds: Int = 3600,
        private val refreshTokenConverter: TokenConverter<RefreshToken>? = null
) : AccessTokenConverter {
    override fun convertToToken(username: String, clientId: String, requestedScopes: Set<String>): AccessToken {
        var refreshToken: RefreshToken? = null

        if (refreshTokenConverter != null) {
            refreshToken = refreshTokenConverter.convertToToken(username, clientId, requestedScopes)
        }

        return convertToToken(username, clientId, requestedScopes, refreshToken)
    }

    override fun convertToToken(username: String, clientId: String, requestedScopes: Set<String>, refreshToken: RefreshToken?): AccessToken {
        return AccessToken(
                UUID.randomUUID().toString(),
                "bearer",
                Instant.now().plusSeconds(accessTokenExpireInSeconds.toLong()),
                username,
                clientId,
                requestedScopes,
                refreshToken
        )
    }
}