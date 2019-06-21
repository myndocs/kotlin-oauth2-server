package nl.myndocs.oauth2.request.auth

import nl.myndocs.oauth2.authenticator.Credentials
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.headerCaseInsensitive

open class BasicAuthenticator(protected val context: CallContext) {
    fun extractCredentials(): Credentials? {
        val authorizationHeader = context.headerCaseInsensitive("authorization") ?: ""

        return BasicAuth.parseCredentials(authorizationHeader)
    }

    fun openAuthenticationDialog() {
        context.respondHeader("WWW-Authenticate", "Basic realm=\"${context.queryParameters["client_id"]}\"")
    }
}