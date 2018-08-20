package nl.myndocs.oauth2.ktor.feature.routing.authorize

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.BasicAuth
import nl.myndocs.oauth2.request.RedirectTokenRequest

suspend fun PipelineContext<Unit, ApplicationCall>.configureImplicitTokenGranting(feature: Oauth2ServerFeature) {
    val queryParameters = call.request.queryParameters

    val authorizationHeader = call.request.header("authorization") ?: ""
    val credentials = BasicAuth.parse(authorizationHeader)

    try {
        val redirect = feature.tokenService.redirect(
                RedirectTokenRequest(
                        queryParameters["client_id"],
                        queryParameters["redirect_uri"],
                        credentials.username ?: "",
                        credentials.password ?: "",
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
        call.response.header("WWW-Authenticate", "Basic realm=\"${queryParameters["client_id"]}\"")
        call.respond(HttpStatusCode.Unauthorized)
        finish()
        return
    }
}
