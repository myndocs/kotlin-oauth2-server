package nl.myndocs.oauth2.javalin

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.javalin.request.JavalinCallContext


fun Javalin.enableOauthServer(configurationCallback: ConfigurationBuilder.Configuration.() -> Unit) {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    this.routes {
        path(callRouter.tokenEndpoint) {
            post { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }

        path(callRouter.authorizeEndpoint) {
            get { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }

        path(callRouter.tokenInfoEndpoint) {
            get { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext, configuration.authorizerFactory(javalinCallContext))
            }
        }
    }
}
