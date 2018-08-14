package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respondText
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.token.AccessToken
import java.util.*

// @TODO: Move logic to core
suspend fun PipelineContext<Unit, ApplicationCall>.configureCodeConsumer(feature: Oauth2ServerFeature, formParams: Parameters) {
    val requiredParameters = arrayOf("code", "redirect_uri", "client_id")

    for (requiredParameter in requiredParameters) {
        if (formParams[requiredParameter] == null) {
            call.respondText(text = "'$requiredParameter' not given", status = HttpStatusCode.BadRequest)
            finish()
            return
        }
    }

    val code = formParams["code"]!!
    val redirectUri = formParams["redirect_uri"]!!
    val clientId = formParams["client_id"]!!
    val clientSecret = formParams["client_secret"] ?: ""

    val consumeCodeToken = feature.tokenStore.consumeCodeToken(code)

    if (consumeCodeToken == null) {
        call.respondText(text = "'code' is invalid", status = HttpStatusCode.BadRequest)
        finish()
        return
    }


    val clientService = feature.clientService
    val client = clientService.clientOf(clientId)

    if (consumeCodeToken.redirectUri != redirectUri || consumeCodeToken.clientId != clientId || !clientService.validClient(client!!, clientSecret)) {
        call.respondText(text = "could not verify token", status = HttpStatusCode.BadRequest)
        finish()
        return
    }

    // @TODO: should not be done here
    val accessToken = AccessToken(
            UUID.randomUUID().toString(),
            "bearer",
            3600,
            consumeCodeToken.username,
            consumeCodeToken.clientId,
            consumeCodeToken.scopes,
            UUID.randomUUID().toString()
    )

    feature.tokenStore.storeAccessToken(accessToken)

    call.respondText(
            """
                                {
                                  "access_token": "${accessToken.accessToken}",
                                  "token_type": "${accessToken.tokenType}",
                                  "expires_in": ${accessToken.expiresIn},
                                  "refresh_token": "${accessToken.refreshToken}"
                                }
                            """.trimIndent(),
            io.ktor.http.ContentType.Application.Json
    )

}