package nl.myndocs.oauth2.ktor.feature.routing.authorize

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.request.RedirectTokenRequest

suspend fun PipelineContext<Unit, ApplicationCall>.configureImplicitTokenGranting(feature: Oauth2ServerFeature) {
    val queryParameters = call.request.queryParameters

    val credentials = feature.authenticator.authenticate(call)

    try {
        val redirect = feature.tokenService.redirect(
                RedirectTokenRequest(
                        queryParameters["client_id"],
                        queryParameters["redirect_uri"],
                        credentials?.username ?: "",
                        credentials?.password ?: "",
                        queryParameters["scope"]
                )
        )


        call.respondRedirect(
                queryParameters["redirect_uri"] + "#access_token=${redirect.accessToken}" +
                        "&token_type=bearer&expires_in=${redirect.expiresIn()}"
        )

        finish()
        return
    } catch (unverifiedIdentityException: InvalidIdentityException) {
        feature.authenticator.failedAuthentication(call)
        call.respond(HttpStatusCode.Unauthorized)
        finish()
        return
    }
}
