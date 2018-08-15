package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant
import java.util.*

class UUIDRefreshTokenConverter(
        private val refreshTokenExpireInSeconds: Int = 86400
) : RefreshTokenConverter {
    override fun convertToToken(username: String, clientId: String, requestedScopes: Set<String>): RefreshToken {
        return RefreshToken(
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(refreshTokenExpireInSeconds.toLong()),
                username,
                clientId,
                requestedScopes
        )
    }
}