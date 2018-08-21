package nl.myndocs.oauth2.ktor.feature.routing.token

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.toJson

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

        try {
            val allowedGrantTypes = setOf("password", "authorization_code", "refresh_token")
            val grantType = formParams["grant_type"] ?: throw InvalidRequestException("'grant_type' not given")

            if (!allowedGrantTypes.contains(grantType)) {
                throw InvalidGrantException("'grant_type' with value '$grantType' not allowed")
            }

            when (grantType) {
                "password" -> configurePasswordGrantRouting(feature, formParams)
                "authorization_code" -> configureCodeConsumer(feature, formParams)
                "refresh_token" -> configureRefreshToken(feature, formParams)
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
