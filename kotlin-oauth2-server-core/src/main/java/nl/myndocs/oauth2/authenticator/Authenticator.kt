package nl.myndocs.oauth2.authenticator


interface Authenticator<T : Any> {
    /**
     * Call to retrieve the credentials from Context
     * If credentials can not be retrieved from Context return null
     */
    fun authenticate(context: T): Credentials?

    /**
     * Callback when authentication have failed
     */
    fun failedAuthentication(context: T)
}