package nl.myndocs.oauth2.token

import java.time.Instant
import java.time.temporal.ChronoUnit

interface ExpirableToken {
    val expireTime: Instant

    fun expiresIn(): Int =
            Instant.now().until(expireTime, ChronoUnit.SECONDS).toInt()

    fun expired(): Boolean =
            expiresIn() <= 0
}