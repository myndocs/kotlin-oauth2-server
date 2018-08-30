package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant
import java.util.*

class UUIDAccessTokenConverter(
        private val accessTokenExpireInSeconds: Int = 3600
) : AccessTokenConverter {

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