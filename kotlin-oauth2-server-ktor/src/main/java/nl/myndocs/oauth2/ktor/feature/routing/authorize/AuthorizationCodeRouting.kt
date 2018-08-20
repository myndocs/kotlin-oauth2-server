package nl.myndocs.oauth2.ktor.feature.routing.authorize

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import nl.myndocs.oauth2.exception.InvalidIdentityException
import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.BasicAuth
import nl.myndocs.oauth2.ktor.feature.util.toJson
import nl.myndocs.oauth2.request.RedirectAuthorizationCodeRequest

suspend fun PipelineContext<Unit, ApplicationCall>.configureAuthorizationCodeGranting(feature: Oauth2ServerFeature) {
    if (call.request.httpMethod != HttpMethod.Get) {
        proceed()
        return
    }

    val requestPath = call.request.path()
    if (requestPath != feature.authorizeEndpoint) {
        proceed()
        return
    }

    val queryParameters = call.request.queryParameters

    val authorizationHeader = call.request.header("authorization") ?: ""
    val credentials = BasicAuth.parse(authorizationHeader)

    try {
        val redirect = feature.tokenService.redirect(
                RedirectAuthorizationCodeRequest(
                        queryParameters["client_id"],
                        queryParameters["redirect_uri"],
                        credentials.username ?: "",
                        credentials.password ?: "",
                        queryParameters["scope"]
                )
        )


        call.respondRedirect(
                queryParameters["redirect_uri"] + "?code=${redirect.codeToken}"
        )

        finish()
        return
    } catch (unverifiedIdentityException: InvalidIdentityException) {
        call.response.header("WWW-Authenticate", "Basic realm=\"${queryParameters["client_id"]}\"")
        call.respond(HttpStatusCode.Unauthorized)
        finish()
        return
    } catch (oauthException: OauthException) {
        call.respondText(text = oauthException.toJson(), status = HttpStatusCode.BadRequest)
        finish()
        return
    }
}

