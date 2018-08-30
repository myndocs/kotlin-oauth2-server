package nl.myndocs.oauth2.token

import java.time.Instant

data class AccessToken(
        val accessToken: String,
        val tokenType: String,
        override val expireTime: Instant,
        val username: String,
        val clientId: String,
        val scopes: Set<String>,
        val refreshToken: RefreshToken?
) : ExpirableToken