package nl.myndocs.oauth2.token

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.client.CodeChallengeMethod
import java.time.Instant

data class CodeToken(
    val codeToken: String,
    override val expireTime: Instant,
    val identity: Identity,
    val clientId: String,
    val redirectUri: String,
    val scopes: Set<String>,
    val codeChallenge: String? = null,
    val codeChallengeMethod: CodeChallengeMethod? = null
) : ExpirableToken