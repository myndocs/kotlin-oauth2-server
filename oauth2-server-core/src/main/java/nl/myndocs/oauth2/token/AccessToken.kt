package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.identity.Identity
import java.time.Instant

data class AccessToken(
    val accessToken: String,
    val tokenType: String,
    override val expireTime: Instant,
    val identity: Identity?,
    val clientId: String,
    val scopes: Set<String>,
    val refreshToken: RefreshToken?
) : ExpirableToken