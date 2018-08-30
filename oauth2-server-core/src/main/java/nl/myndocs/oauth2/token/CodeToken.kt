package nl.myndocs.oauth2.token

import java.time.Instant

data class CodeToken(
        val codeToken: String,
        override val expireTime: Instant,
        val username: String,
        val clientId: String,
        val redirectUri: String,
        val scopes: Set<String>
) : ExpirableToken