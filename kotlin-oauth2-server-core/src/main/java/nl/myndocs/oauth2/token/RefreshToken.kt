package nl.myndocs.oauth2.token

import java.time.Instant

data class RefreshToken(
        val refreshToken: String,
        override val expireTime: Instant,
        val username: String,
        val clientId: String,
        val scopes: Set<String>
) : ExpirableToken