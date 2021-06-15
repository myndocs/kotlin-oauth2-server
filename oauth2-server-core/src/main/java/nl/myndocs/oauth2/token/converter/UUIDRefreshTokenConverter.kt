package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.RefreshToken
import java.time.Instant
import java.util.*

class UUIDRefreshTokenConverter(
    private val refreshTokenExpireInSeconds: Int = 86400
) : RefreshTokenConverter {
    override fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>): RefreshToken {
        return RefreshToken(
            UUID.randomUUID().toString(),
            Instant.now().plusSeconds(refreshTokenExpireInSeconds.toLong()),
            identity,
            clientId,
            requestedScopes
        )
    }
}