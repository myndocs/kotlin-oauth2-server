package nl.myndocs.oauth2.ktor.feature

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import io.ktor.util.AttributeKey
import nl.myndocs.oauth2.Authorizer
import nl.myndocs.oauth2.requeset.PasswordGrantRequest
import java.util.*

class Oauth2ServerFeature(configuration: Configuration) {
    val tokenEndpoint = configuration.tokenEndpoint
    val authorizer = configuration.authorizer!!

    class Configuration {
        var tokenEndpoint = "/oauth/token"
        var authorizer: Authorizer? = null
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Oauth2ServerFeature.Configuration, Oauth2ServerFeature> {
        override val key = AttributeKey<Oauth2ServerFeature>("Oauth2ServerFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): Oauth2ServerFeature {

            val configuration = Oauth2ServerFeature.Configuration().apply(configure)

            val feature = Oauth2ServerFeature(configuration)

            pipeline.intercept(ApplicationCallPipeline.Infrastructure) {

                try {
                    if (call.request.httpMethod != HttpMethod.Post) {
                        proceed()
                        return@intercept
                    }

                    if (call.request.path() != feature.tokenEndpoint) {
                        proceed()
                        return@intercept
                    }

                    val params = call.receiveParameters()

                    val authorizationHeader = call.request.header("authorization")!!

                    authorizationHeader.startsWith("basic ", true)

                    val basicAuthorizationString = String(
                            Base64.getDecoder()
                                    .decode(authorizationHeader.substring(6))
                    )


                    val (clientId, clientSecret) = basicAuthorizationString.split(":")
                    val authorize = feature.authorizer.authorize(
                            PasswordGrantRequest(
                                    clientId,
                                    clientSecret,
                                    params["username"]!!,
                                    params["password"]!!,
                                    params["scope"]
                            )
                    )

                    call.respondText(
                            """
                                {
                                  "access_token": "${authorize.accessToken}",
                                  "token_type": "${authorize.tokenType}",
                                  "expires_in": ${authorize.expiresIn},
                                  "refresh_token": "${authorize.refreshToken}"
                                }
                            """.trimIndent(),
                            io.ktor.http.ContentType.Application.Json
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                }

                finish()
            }

            return feature
        }
    }
}
