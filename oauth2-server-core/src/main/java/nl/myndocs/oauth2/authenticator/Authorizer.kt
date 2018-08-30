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

    /**
     * Override default token authentication if Authenticator != null
     * This allows different authentication per request
     */
    fun authenticator(): Authenticator? = null

    /**
     * Override default token scope verification if IdentityScopeVerifier != null
     * This allows scopes verification per request
     * E.g. could be used to make consent pages
     */
    fun scopesVerifier(): IdentityScopeVerifier? = null
}