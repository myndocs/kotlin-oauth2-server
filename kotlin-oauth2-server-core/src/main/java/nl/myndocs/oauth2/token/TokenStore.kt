package nl.myndocs.oauth2.token

interface TokenStore {
    fun storeAccessToken(accessToken: AccessToken)

    fun accessToken(token: String): AccessToken?

    fun storeCodeToken(codeToken: CodeToken)

    fun codeToken(token: String): CodeToken?

    /**
     * Retrieve token and delete it from store
     */
    fun consumeCodeToken(token: String): CodeToken?
}