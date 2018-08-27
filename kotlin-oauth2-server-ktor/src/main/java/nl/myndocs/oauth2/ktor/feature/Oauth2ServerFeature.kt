package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.config.Configuration
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext
import nl.myndocs.oauth2.request.CallContext

class Oauth2ServerFeature(configuration: Configuration<TokenService>) {
    val callRouter = configuration.callRouter

    val authorizerFactory: (CallContext) -> Authorizer = configuration.authorizerFactory


    companion object Feature : ApplicationFeature<ApplicationCallPipeline, ConfigurationBuilder.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: ConfigurationBuilder.Configuration.() -> Unit): Oauth2ServerFeature {
            val configuration = ConfigurationBuilder.build(configure)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Infrastructure) {
                val ktorCallContext = KtorCallContext(call)
                val authorizer = feature.authorizerFactory(ktorCallContext)

                feature.callRouter.route(ktorCallContext, authorizer)
            }

            return feature
        }
    }
}
