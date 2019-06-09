package nl.myndocs.oauth2.authenticator


interface Authorizer {
    /**
     * Call to retrieve the credentials from Context
     * If credentials can not be retrieved from Context return null
     */
    fun extractCredentials(): Credentials?

    /**
     * Callback when authentication have failed
     */
    fun failedAuthentication()
}