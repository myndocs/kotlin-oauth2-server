package nl.myndocs.oauth2.http4k

import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.http4k.request.Http4kCallContext
import nl.myndocs.oauth2.http4k.response.ResponseBuilder
import nl.myndocs.oauth2.request.auth.CallContextBasicAuthenticator
import nl.myndocs.oauth2.router.RedirectRouter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun RoutingHttpHandler.enableOauth2(
        authenticationCallback: (Request, RedirectRouter) -> Response = { request, callRouter ->
            val responseBuilder = ResponseBuilder()
            val callContext = Http4kCallContext(request, responseBuilder)

            CallContextBasicAuthenticator.handleAuthentication(callContext, callRouter)

            responseBuilder.build()
        },
        configurationCallback: ConfigurationBuilder.Configuration.() -> Unit
): RoutingHttpHandler {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    return routes(
            this,
            callRouter.tokenEndpoint bind Method.POST to { request: Request ->
                val responseBuilder = ResponseBuilder()
                val callContext = Http4kCallContext(request, responseBuilder)
                callRouter.route(callContext)

                responseBuilder.build()
            },
            callRouter.authorizeEndpoint bind Method.GET to { request: Request ->
                authenticationCallback(request, callRouter)
            },
            callRouter.tokenInfoEndpoint bind Method.GET to { request: Request ->
                val responseBuilder = ResponseBuilder()
                val callContext = Http4kCallContext(request, responseBuilder)
                callRouter.route(callContext)

                responseBuilder.build()
            }
    )
}