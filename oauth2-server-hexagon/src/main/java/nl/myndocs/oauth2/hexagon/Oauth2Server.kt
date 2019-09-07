package nl.myndocs.oauth2.hexagon

import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Router
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.hexagon.request.HexagonCallContext
import nl.myndocs.oauth2.request.auth.CallContextBasicAuthenticator
import nl.myndocs.oauth2.router.RedirectRouter


fun Router.enableOauthServer(
        authenticationCallback: (Call, RedirectRouter) -> Unit = { call, callRouter ->
            val context = HexagonCallContext(call)
            CallContextBasicAuthenticator.handleAuthentication(context, callRouter)
        },
        configurationCallback: ConfigurationBuilder.Configuration.() -> Unit
) {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    post(callRouter.tokenEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }

    get(callRouter.authorizeEndpoint) {
        authenticationCallback(this, callRouter)
    }

    post(callRouter.authorizeEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }

    get(callRouter.tokenInfoEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }
}
