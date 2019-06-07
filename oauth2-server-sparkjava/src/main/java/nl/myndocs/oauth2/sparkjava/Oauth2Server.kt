package nl.myndocs.oauth2.sparkjava

import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.sparkjava.request.SparkjavaCallContext
import spark.Spark.get
import spark.Spark.post

object Oauth2Server {
    fun configureOauth2Server(configurationCallback: ConfigurationBuilder.Configuration.() -> Unit) {
        val configuration = ConfigurationBuilder.build(configurationCallback)

        val callRouter = configuration.callRouter

        post(callRouter.tokenEndpoint) { req, res ->
            val sparkjavaCallContext = SparkjavaCallContext(req, res)
            callRouter.route(sparkjavaCallContext)

            res.body()
        }


        get(callRouter.authorizeEndpoint) { req, res ->
            val sparkjavaCallContext = SparkjavaCallContext(req, res)
            callRouter.route(sparkjavaCallContext)

            res.body()
        }

        get(callRouter.tokenInfoEndpoint) { req, res ->
            val sparkjavaCallContext = SparkjavaCallContext(req, res)
            callRouter.route(sparkjavaCallContext)

            res.body()
        }
    }
}