package nl.myndocs.oauth2.tokenstore.inmemory

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.Token
import nl.myndocs.oauth2.token.TokenStore
import java.util.*

class InMemoryTokenStore : TokenStore {
    private val tokens = mutableMapOf<String, StoredToken>()
    private val codes = mutableMapOf<String, StoredCode>()

    override fun generateAndStoreTokenFor(identity: Identity, client: Client, requestedScopes: Set<String>): Token {
        val token = Token(
                UUID.randomUUID().toString(),
                "bearer",
                3600,
                UUID.randomUUID().toString()
        )

        tokens[token.accessToken] = StoredToken(
                token,
                identity,
                client,
                requestedScopes
        )

        return token
    }

    override fun generateCodeTokenAndStoreFor(identity: Identity, client: Client, redirectUri: String, requestedScopes: Set<String>): String {
        val code = UUID.randomUUID().toString()

        codes[code] = StoredCode(
                code,
                identity,
                client,
                redirectUri,
                requestedScopes
        )

        return code
    }

    class StoredToken(
            val token: Token,
            val identity: Identity,
            val client: Client,
            val scopes: Set<String>
    )

    class StoredCode(
            val code: String,
            val identity: Identity,
            val client: Client,
            val redirectUri: String,
            val scopes: Set<String>
    )
}
