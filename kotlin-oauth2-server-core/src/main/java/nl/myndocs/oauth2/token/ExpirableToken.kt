package nl.myndocs.oauth2.token

import java.time.Instant
import java.time.temporal.ChronoUnit

interface ExpirableToken {
    val expireTime: Instant

    val expiresIn: Int
        get() = Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()

    val expired: Boolean
        get() = expiresIn <= 0
}