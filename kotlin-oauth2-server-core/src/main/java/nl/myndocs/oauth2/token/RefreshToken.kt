package nl.myndocs.oauth2.token

import java.time.Instant
import java.time.temporal.ChronoUnit

data class RefreshToken(
        val refreshToken: String,
        val expireTime: Instant,
        val username: String,
        val clientId: String,
        val scopes: Set<String>
) {
    val expiresIn: Int
        get() = Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()
}