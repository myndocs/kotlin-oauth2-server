package nl.myndocs.oauth2.token

interface TokenStore {
    fun storeAccessToken(accessToken: AccessToken)

    fun accessToken(token: String): AccessToken?

    fun storeCodeToken(codeToken: CodeToken)

    fun codeToken(token: String): CodeToken?
}