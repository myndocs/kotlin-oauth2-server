package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature

suspend fun PipelineContext<Unit, ApplicationCall>.configureTokenEndpoint(feature: Oauth2ServerFeature) {
    try {
        if (call.request.httpMethod != HttpMethod.Post) {
            proceed()
            return
        }

        if (call.request.path() != feature.tokenEndpoint) {
            proceed()
            return
        }
        val formParams = call.receiveParameters()

        val allowedGrantTypes = setOf("password", "implicit", "authorization_code")
        val grantType = formParams["grant_type"]
        if (grantType == null) {
            call.respond(HttpStatusCode.BadRequest, "'grant_type' not given")
            finish()
            return
        }

        if (!allowedGrantTypes.contains(grantType)) {
            call.respond(HttpStatusCode.BadRequest, "'grant_type' with value '$grantType' not allowed")
            finish()
            return
        }

        when (grantType) {
            "password" -> configurePasswordGrantRouting(feature, formParams)
            "authorization_code" -> configureCodeConsumer(feature, formParams)
        }

    } catch (t: Throwable) {
        t.printStackTrace()
    }
}
