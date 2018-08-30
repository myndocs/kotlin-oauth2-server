package nl.myndocs.oauth2.request.auth

import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.authenticator.Credentials
import nl.myndocs.oauth2.request.CallContext

open class BasicAuthorizer(protected val context: CallContext) : Authorizer {
    override fun extractCredentials(): Credentials? {
        val authorizationHeader = context.headers["authorization"] ?: ""
        return BasicAuth.parseCredentials(authorizationHeader)
    }

    override fun failedAuthentication() {
        context.respondHeader("WWW-Authenticate", "Basic realm=\"${context.queryParameters["client_id"]}\"")
    }
}