package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.response.respondText
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.toJson
import nl.myndocs.oauth2.request.PasswordGrantRequest
import java.util.*

suspend fun PipelineContext<Unit, ApplicationCall>.configurePasswordGrantRouting(feature: Oauth2ServerFeature, formParams: Parameters) {
    val authorizationHeader = call.request.header("authorization")!!

    authorizationHeader.startsWith("basic ", true)

    val basicAuthorizationString = String(
            Base64.getDecoder()
                    .decode(authorizationHeader.substring(6))
    )


    val (clientId, clientSecret) = basicAuthorizationString.split(":")
    val tokenResponse = feature.tokenService.authorize(
            PasswordGrantRequest(
                    clientId,
                    clientSecret,
                    formParams["username"]!!,
                    formParams["password"]!!,
                    formParams["scope"]
            )
    )

    call.respondText(
            tokenResponse.toJson(),
            io.ktor.http.ContentType.Application.Json
    )

    finish()
}