package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.Authorizer
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.ktor.feature.routing.authorize.configureAuthorizationCodeGranting
import nl.myndocs.oauth2.ktor.feature.routing.token.configureTokenEndpoint
import nl.myndocs.oauth2.token.TokenStore

class Oauth2ServerFeature(configuration: Configuration) {
    val tokenEndpoint = configuration.tokenEndpoint
    val authorizeEndpoint = configuration.authorizeEndpoint
    val clientService = configuration.clientService!!
    val identityService = configuration.identityService!!
    val tokenStore = configuration.tokenStore!!
    val authorizer = Authorizer(identityService, clientService, tokenStore)

    class Configuration {
        var tokenEndpoint = "/oauth/token"
        var authorizeEndpoint = "/oauth/authorize"
        var clientService: ClientService? = null
        var identityService: IdentityService? = null
        var tokenStore: TokenStore? = null
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Oauth2ServerFeature.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Oauth2ServerFeature {

            val configuration = Oauth2ServerFeature.Configuration().apply(configure)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Infrastructure) {
                configureTokenEndpoint(feature)
                configureAuthorizationCodeGranting(feature)
            }

            return feature
        }
    }
}
