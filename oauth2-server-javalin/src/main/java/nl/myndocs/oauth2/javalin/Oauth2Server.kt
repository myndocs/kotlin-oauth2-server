package nl.myndocs.oauth2.javalin

import io.javalin.Context
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.javalin.request.JavalinCallContext
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.router.RedirectRouter


fun Javalin.enableOauthServer(
        authenticationCallback: (Context, RedirectRouter) -> Unit = { ctx, callRouter ->
            val context = JavalinCallContext(ctx)
            val basicAuthorizer = BasicAuthorizer(context)
            if (basicAuthorizer.extractCredentials() == null) {
                basicAuthorizer.openAuthenticationDialog()
            } else {
                callRouter.route(context, basicAuthorizer.extractCredentials())
                        .also { if (!it.successfulLogin) { basicAuthorizer.openAuthenticationDialog() } }
            }
        },
        configurationCallback: ConfigurationBuilder.Configuration.() -> Unit
) {
    val configuration = ConfigurationBuilder.build(configurationCallback)

    val callRouter = configuration.callRouter

    this.routes {
        path(callRouter.tokenEndpoint) {
            post { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext)
            }
        }

        path(callRouter.authorizeEndpoint) {
            get { ctx ->
                authenticationCallback(ctx, callRouter)
            }

            post { ctx ->
                authenticationCallback(ctx, callRouter)
            }
        }

        path(callRouter.tokenInfoEndpoint) {
            get { ctx ->
                val javalinCallContext = JavalinCallContext(ctx)
                callRouter.route(javalinCallContext)
            }
        }
    }
}
