package nl.myndocs.oauth2.token

import java.time.Instant
import java.time.temporal.ChronoUnit

data class AccessToken(
        val accessToken: String,
        // @TODO: tokenType is misleading. this is about header Bearer not REFRESH_TOKEN
        val tokenType: String,
        val expireTime: Instant,
        val username: String,
        val clientId: String,
        val scopes: Set<String>,
        val refreshToken: RefreshToken?
) {
    val expiresIn: Int
        get() = Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()
}