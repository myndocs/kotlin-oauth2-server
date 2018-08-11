package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.AttributeKey

class Oauth2ServerFeature(configuration: Configuration) {
    val tokenEndpoint = configuration.tokenEndpoint

    class Configuration {
        var tokenEndpoint = "/oauth/token"
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Oauth2ServerFeature.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Oauth2ServerFeature {

            val configuration = Oauth2ServerFeature.Configuration().apply(configure)

            val feature = Oauth2ServerFeature(configuration)

            val phase = pipeline.items.find { it.name == "Infrastructure" }

            pipeline.intercept(phase!!) {
                this.context.application.routing {
                    get(feature.tokenEndpoint) {
                    }
                }

            }

            return feature
        }
    }
}
