package nl.myndocs.oauth2.token.converter

import nl.myndocs.oauth2.client.CodeChallengeMethod
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.CodeToken

interface CodeTokenConverter {
    fun convertToToken(
            identity: Identity,
            clientId: String,
            redirectUri: String,
            requestedScopes: Set<String>
    ): CodeToken {
        throw NotImplementedError("CodeTokenConverter must implement " +
                "convertToToken(Identity, String, String?, CodeChallengeMethod?, String, Set<String>): CodeToken")
    }

    fun convertToToken(
            identity: Identity,
            clientId: String,
            codeChallenge: String?,
            codeChallengeMethod: CodeChallengeMethod?,
            redirectUri: String,
            requestedScopes: Set<String>
    ): CodeToken {
        if (codeChallenge != null || codeChallengeMethod != null) {
            throw IllegalStateException("CodeTokenConverter must implement " +
                    "convertToToken(Identity, String, String?, CodeChallengeMethod?, String, Set<String>): CodeToken")
        }

        return convertToToken(
                identity = identity,
                clientId = clientId,
                redirectUri = redirectUri,
                requestedScopes = requestedScopes
        )
    }
}