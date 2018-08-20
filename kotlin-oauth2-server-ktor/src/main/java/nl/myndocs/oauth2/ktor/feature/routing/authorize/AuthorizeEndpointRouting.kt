package nl.myndocs.oauth2.ktor.feature.routing.authorize

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.toJson

suspend fun PipelineContext<Unit, ApplicationCall>.configureAuthorizeEndpoint(feature: Oauth2ServerFeature) {
    try {
        if (call.request.httpMethod != HttpMethod.Get) {
            proceed()
            return
        }

        val requestPath = call.request.path()
        if (requestPath != feature.authorizeEndpoint) {
            proceed()
            return
        }

        val allowedResponseTypes = setOf("code", "token")
        val responseType = call.request.queryParameters["response_type"]

        if (responseType == null) {
            call.respond(HttpStatusCode.BadRequest, "'response_type' not given")
            finish()
            return
        }

        if (!allowedResponseTypes.contains(responseType)) {
            call.respond(HttpStatusCode.BadRequest, "'response_type' with value '$responseType' not allowed")
            finish()
            return
        }

        try {
            when (responseType) {
                "code" -> configureAuthorizationCodeGranting(feature)
                "token" -> configureImplicitTokenGranting(feature)
            }
        } catch (oauthException: OauthException) {
            call.respondText(text = oauthException.toJson(), status = HttpStatusCode.BadRequest)
            finish()
            return
        }

    } catch (t: Throwable) {
        t.printStackTrace()
    }
}
