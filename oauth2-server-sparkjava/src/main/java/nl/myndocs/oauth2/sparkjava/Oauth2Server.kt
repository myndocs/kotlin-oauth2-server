package nl.myndocs.oauth2.sparkjava

import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.request.auth.BasicAuthorizer
import nl.myndocs.oauth2.router.RedirectRouter
import nl.myndocs.oauth2.sparkjava.request.SparkjavaCallContext
import spark.Request
import spark.Response
import spark.Spark.get
import spark.Spark.post

object Oauth2Server {
    fun configureOauth2Server(
            authenticationCallback: (Request, Response, RedirectRouter) -> Unit = { request, response, callRouter ->
                val context = SparkjavaCallContext(request, response)
                val basicAuthorizer = BasicAuthorizer(context)
                if (basicAuthorizer.extractCredentials() == null) {
                    basicAuthorizer.failedAuthentication()
                } else {
                    callRouter.route(context, basicAuthorizer.extractCredentials())
                }
            },
            configurationCallback: ConfigurationBuilder.Configuration.() -> Unit
    ) {
        val configuration = ConfigurationBuilder.build(configurationCallback)

        val callRouter = configuration.callRouter

        post(callRouter.tokenEndpoint) { req, res ->
            val sparkjavaCallContext = SparkjavaCallContext(req, res)
            callRouter.route(sparkjavaCallContext)

            res.body()
        }


        get(callRouter.authorizeEndpoint) { req, res ->
            authenticationCallback(req, res, callRouter)

            res.body()
        }

        post(callRouter.authorizeEndpoint) { req, res ->
            authenticationCallback(req, res, callRouter)

            res.body()
        }

        get(callRouter.tokenInfoEndpoint) { req, res ->
            val sparkjavaCallContext = SparkjavaCallContext(req, res)
            callRouter.route(sparkjavaCallContext)

            res.body()
        }
    }
}