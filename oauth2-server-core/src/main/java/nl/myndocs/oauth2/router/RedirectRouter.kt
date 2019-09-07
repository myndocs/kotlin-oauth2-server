package nl.myndocs.oauth2.router

import nl.myndocs.oauth2.authenticator.Credentials
import nl.myndocs.oauth2.request.CallContext

interface RedirectRouter {
    fun route(callContext: CallContext, credentials: Credentials?): RedirectRouterResponse
}