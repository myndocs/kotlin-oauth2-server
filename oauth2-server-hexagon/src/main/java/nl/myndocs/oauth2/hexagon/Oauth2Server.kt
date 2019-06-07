package nl.myndocs.oauth2.hexagon

import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.hexagon.request.HexagonCallContext


fun Router.enableOauthServer(configurationCallback: ConfigurationBuilder.Configuration.() -> Unit) {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    post(callRouter.tokenEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }

    get(callRouter.authorizeEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }

    post(callRouter.authorizeEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }

    get(callRouter.tokenInfoEndpoint) {
        callRouter.route(HexagonCallContext(this))
    }
}
