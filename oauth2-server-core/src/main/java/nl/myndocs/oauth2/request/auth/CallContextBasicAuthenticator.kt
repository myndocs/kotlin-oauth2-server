package nl.myndocs.oauth2.request.auth

import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.router.RedirectRouter

object CallContextBasicAuthenticator {
    fun handleAuthentication(context: CallContext, router: RedirectRouter) = with(BasicAuthenticator(context)) {
        router.route(context, this.extractCredentials()).also { response ->
            if (!response.successfulLogin)
                openAuthenticationDialog()
        }
    }
}