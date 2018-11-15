package nl.myndocs.oauth2.http4k

import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.http4k.request.Http4kCallContext
import nl.myndocs.oauth2.http4k.response.ResponseBuilder
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

infix fun RoutingHttpHandler.`enable oauth2`(configurationCallback: ConfigurationBuilder.Configuration.() -> Unit): RoutingHttpHandler {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    return routes(
            this,
            callRouter.tokenEndpoint bind Method.POST to { request: Request ->
                val responseBuilder = ResponseBuilder()
                val callContext = Http4kCallContext(request, responseBuilder)
                callRouter.route(callContext, configuration.authorizerFactory(callContext))

                responseBuilder.build()
            },
            callRouter.authorizeEndpoint bind Method.GET to { request: Request ->
                val responseBuilder = ResponseBuilder()
                val callContext = Http4kCallContext(request, responseBuilder)
                callRouter.route(callContext, configuration.authorizerFactory(callContext))

                responseBuilder.build()
            },
            callRouter.tokenInfoEndpoint bind Method.GET to { request: Request ->
                val responseBuilder = ResponseBuilder()
                val callContext = Http4kCallContext(request, responseBuilder)
                callRouter.route(callContext, configuration.authorizerFactory(callContext))

                responseBuilder.build()
            }
    )
}