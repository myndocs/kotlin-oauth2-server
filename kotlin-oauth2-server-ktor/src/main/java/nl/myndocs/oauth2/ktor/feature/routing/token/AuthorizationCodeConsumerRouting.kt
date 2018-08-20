package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respondText
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.toJson
import nl.myndocs.oauth2.request.AuthorizationCodeRequest

suspend fun PipelineContext<Unit, ApplicationCall>.configureCodeConsumer(feature: Oauth2ServerFeature, formParams: Parameters) {
    val accessToken = feature.tokenService.authorize(
            AuthorizationCodeRequest(
                    formParams["client_id"],
                    formParams["client_secret"],
                    formParams["code"],
                    formParams["redirect_uri"]
            )
    )

    call.respondText(
            accessToken.toJson(),
            io.ktor.http.ContentType.Application.Json
    )
}