package nl.myndocs.oauth2.token

import java.time.Instant
import java.time.temporal.ChronoUnit

data class CodeToken(
        val codeToken: String,
        // @TODO: Is this according spec?
        val expireTime: Instant,
        val username: String,
        val clientId: String,
        val redirectUri: String,
        val scopes: Set<String>
) {
    val expiresIn: Int
        get() = Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()
}