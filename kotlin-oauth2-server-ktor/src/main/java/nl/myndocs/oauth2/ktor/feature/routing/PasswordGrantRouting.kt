package nl.myndocs.oauth2.ktor.feature.routing

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.request.PasswordGrantRequest
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.configurePasswordGrantRouting(feature: Oauth2ServerFeature) {
    try {
        if (call.request.httpMethod != HttpMethod.Post) {
            proceed()
            return
        }

        if (call.request.path() != feature.tokenEndpoint) {
            proceed()
            return
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