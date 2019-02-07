package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.identity.Identity
import java.time.Instant

data class CodeToken(
        val codeToken: String,
        override val expireTime: Instant,
        val identity: Identity,
        val clientId: String,
        val redirectUri: String,
        val scopes: Set<String>
) : ExpirableToken