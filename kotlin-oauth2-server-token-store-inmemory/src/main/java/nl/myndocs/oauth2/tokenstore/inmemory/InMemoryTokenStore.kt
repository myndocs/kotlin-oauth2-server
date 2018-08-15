package nl.myndocs.oauth2.tokenstore.inmemory

import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.TokenStore

class InMemoryTokenStore : TokenStore {
    private val accessTokens = mutableMapOf<String, AccessToken>()
    private val codes = mutableMapOf<String, CodeToken>()
    private val refreshTokens = mutableMapOf<String, RefreshToken>()


    override fun storeAccessToken(accessToken: AccessToken) {
        accessTokens[accessToken.accessToken] = accessToken
    }

    override fun accessToken(token: String): AccessToken? = accessTokens[token]

    override fun storeCodeToken(codeToken: CodeToken) {
        codes[codeToken.codeToken] = codeToken
    }

    override fun codeToken(token: String): CodeToken? = codes[token]

    override fun consumeCodeToken(token: String): CodeToken? = codes.remove(token)

    override fun storeRefreshToken(refreshToken: RefreshToken) {
        refreshTokens[refreshToken.refreshToken] = refreshToken
    }

    override fun refreshToken(token: String): RefreshToken? = refreshTokens[token]
}
