package nl.myndocs.oauth2.tokenstore.inmemory

import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.TokenStore

class InMemoryTokenStore : TokenStore {
    private val tokens = mutableMapOf<String, AccessToken>()
    private val codes = mutableMapOf<String, CodeToken>()


    override fun storeAccessToken(accessToken: AccessToken) {
        tokens[accessToken.accessToken] = accessToken
    }

    override fun accessToken(token: String): AccessToken? = tokens[token]

    override fun storeCodeToken(codeToken: CodeToken) {
        codes[codeToken.codeToken] = codeToken
    }

    override fun codeToken(token: String): CodeToken? = codes[token]

    override fun consumeCodeToken(token: String): CodeToken? = codes.remove(token)
}
