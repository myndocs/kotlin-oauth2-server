package nl.myndocs.oauth2.authenticator


interface Authorizer<T : Any> {
    /**
     * Call to retrieve the credentials from Context
     * If credentials can not be retrieved from Context return null
     */
    fun extractCredentials(context: T): Credentials?

    /**
     * Callback when authentication have failed
     */
    fun failedAuthentication(context: T)

    /**
     * Override default token authentication if Authenticator != null
     * This allows different authentication per request
     */
    fun authenticator(context: T): Authenticator? = null

    /**
     * Override default token scope verification if IdentityScopeVerifier != null
     * This allows scopes verification per request
     * E.g. could be used to make consent pages
     */
    fun scopesVerifier(context: T): IdentityScopeVerifier? = null
}