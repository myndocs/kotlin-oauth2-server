package nl.myndocs.oauth2.ktor.feature.config

import io.ktor.application.ApplicationCall
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.router.RedirectRouter

class KtorConfiguration: ConfigurationBuilder.Configuration() {
    var authenticationCallback: (ApplicationCall, RedirectRouter) -> Unit = { call, callRouter ->
        val context = KtorCallContext(call)
        val basicAuthorizer = BasicAuthorizer(context)
        if (basicAuthorizer.extractCredentials() == null) {
            basicAuthorizer.openAuthenticationDialog()
        } else {
            callRouter.route(context, basicAuthorizer.extractCredentials())
                    .also { if (!it.successfulLogin) { basicAuthorizer.openAuthenticationDialog() } }
        }
    }
}