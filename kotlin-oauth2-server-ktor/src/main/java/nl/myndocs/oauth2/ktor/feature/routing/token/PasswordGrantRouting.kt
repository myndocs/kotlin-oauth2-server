package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respondText
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.toJson
import nl.myndocs.oauth2.request.PasswordGrantRequest

suspend fun PipelineContext<Unit, ApplicationCall>.configurePasswordGrantRouting(feature: Oauth2ServerFeature, formParams: Parameters) {
    val tokenResponse = feature.tokenService.authorize(
            PasswordGrantRequest(
                    formParams["client_id"],
                    formParams["client_secret"],
                    formParams["username"],
                    formParams["password"],
                    formParams["scope"]
            )
    )

    call.respondText(
            tokenResponse.toJson(),
            io.ktor.http.ContentType.Application.Json
    )

    finish()
}