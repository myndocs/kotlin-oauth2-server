package nl.myndocs.oauth2.ktor.feature.config

import io.ktor.application.*
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import nl.myndocs.oauth2.request.auth.CallContextBasicAuthenticator
import nl.myndocs.oauth2.router.RedirectRouter

class KtorConfiguration: ConfigurationBuilder.Configuration() {
    var authenticationCallback: (ApplicationCall, RedirectRouter) -> Unit = { call, callRouter ->
        val context = KtorCallContext(call)
        CallContextBasicAuthenticator.handleAuthentication(context, callRouter)
    }
}