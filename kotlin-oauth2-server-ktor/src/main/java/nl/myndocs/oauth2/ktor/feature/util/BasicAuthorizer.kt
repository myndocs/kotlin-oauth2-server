package nl.myndocs.oauth2.ktor.feature.util

import io.ktor.application.ApplicationCall
import io.ktor.request.header
import io.ktor.response.header
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.authenticator.Credentials

object BasicAuthorizer : Authorizer<ApplicationCall> {
    override fun extractCredentials(context: ApplicationCall): Credentials? {
        val authorizationHeader = context.request.header("authorization") ?: ""
        return BasicAuth.parseCredentials(authorizationHeader)
    }

    override fun failedAuthentication(context: ApplicationCall) {
        context.response.header("WWW-Authenticate", "Basic realm=\"${context.request.queryParameters["client_id"]}\"")
    }
}