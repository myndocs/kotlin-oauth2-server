package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.identity.Identity
import java.time.Instant

data class RefreshToken(
        val refreshToken: String,
        override val expireTime: Instant,
        val identity: Identity?,
        val clientId: String,
        val scopes: Set<String>
) : ExpirableToken