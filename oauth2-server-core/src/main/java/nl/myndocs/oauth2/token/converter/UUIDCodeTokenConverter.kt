package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.client.CodeChallengeMethod
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.CodeToken
import java.time.Instant
import java.util.*

class UUIDCodeTokenConverter(
        private val codeTokenExpireInSeconds: Int = 300
) : CodeTokenConverter {
    override fun convertToToken(
            identity: Identity,
            clientId: String,
            codeChallenge: String?,
            codeChallengeMethod: CodeChallengeMethod?,
            redirectUri: String,
            requestedScopes: Set<String>
    ): CodeToken {
        return CodeToken(
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(codeTokenExpireInSeconds.toLong()),
                identity,
                clientId,
                redirectUri,
                requestedScopes,
                codeChallenge,
                codeChallengeMethod
        )
    }
}