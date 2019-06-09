package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.config.Configuration
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.ktor.feature.config.KtorConfiguration
import nl.myndocs.oauth2.ktor.feature.request.KtorCallContext

class Oauth2ServerFeature(configuration: Configuration) {
    val callRouter = configuration.callRouter

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, KtorConfiguration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: KtorConfiguration.() -> Unit): Oauth2ServerFeature {
            val configuration = ConfigurationBuilder.build(configure as ConfigurationBuilder.Configuration.() -> Unit)

            val ktorConfiguration = KtorConfiguration()
            configure(ktorConfiguration)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Features) {
                val ktorCallContext = KtorCallContext(call)

                if (configuration.callRouter.authorizeEndpoint == ktorCallContext.path) {
                    ktorConfiguration.authenticationCallback(call, feature.callRouter)
                }

                if (
                        arrayOf(
                                configuration.callRouter.tokenEndpoint,
                                configuration.callRouter.tokenInfoEndpoint
                        ).contains(ktorCallContext.path)
                ) {
                    feature.callRouter.route(ktorCallContext)
                }
            }

            return feature
        }
    }
}
