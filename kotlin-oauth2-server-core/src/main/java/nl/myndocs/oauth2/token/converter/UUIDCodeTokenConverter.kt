package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.token.CodeToken
import java.time.Instant
import java.util.*

class UUIDCodeTokenConverter(
        private val codeTokenExpireInSeconds: Int = 300
) : CodeTokenConverter {
    override fun convertToToken(username: String, clientId: String, redirectUri: String, requestedScopes: Set<String>): CodeToken {
        return CodeToken(
                UUID.randomUUID().toString(),
                Instant.now().plusSeconds(codeTokenExpireInSeconds.toLong()),
                username,
                clientId,
                redirectUri,
                requestedScopes
        )
    }
}