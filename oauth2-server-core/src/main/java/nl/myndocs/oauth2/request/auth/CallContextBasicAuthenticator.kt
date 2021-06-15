package nl.myndocs.oauth2.request.auth

import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.router.RedirectRouter

object CallContextBasicAuthenticator {
    fun handleAuthentication(callContext: CallContext, callRouter: RedirectRouter) {
        val basicAuthenticator = BasicAuthenticator(callContext)
        if (basicAuthenticator.extractCredentials() == null) {
            basicAuthenticator.openAuthenticationDialog()
        } else {
            callRouter.route(callContext, basicAuthenticator.extractCredentials())
                .also {
                    if (!it.successfulLogin) {
                        basicAuthenticator.openAuthenticationDialog()
                    }
                }
        }
    }
}