package nl.myndocs.oauth2.ktor.feature.routing

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
import nl.myndocs.oauth2.ktor.feature.Oauth2ServerFeature
import nl.myndocs.oauth2.ktor.feature.util.BasicAuth
import nl.myndocs.oauth2.token.CodeToken
import java.util.*

// @TODO: Move logic to core
suspend fun PipelineContext<Unit, ApplicationCall>.configureAuthorizationCodeGranting(feature: Oauth2ServerFeature) {
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

        val queryParameters = call.request.queryParameters

        val requiredParameters = arrayOf("redirect_uri", "client_id", "response_type")


        for (requiredParameter in requiredParameters) {
            if (queryParameters[requiredParameter] == null) {
                call.respondText(text = "'$requiredParameter' not given", status = HttpStatusCode.BadRequest)
                finish()
                return
            }
        }

        val clientOf = feature.clientService
                .clientOf(queryParameters["client_id"]!!)
        if (clientOf?.redirectUris
                        ?.contains(queryParameters["redirect_uri"]) != true
        ) {
            call.respondText(text = "Given 'redirect_uri' with value '${queryParameters["redirect_uri"]}' not allowed to ", status = HttpStatusCode.BadRequest)
            finish()
            return
        }
        val authorizationHeader = call.request.header("authorization")

        if (authorizationHeader == null) {
            call.response.header("WWW-Authenticate", "Basic realm=\"User Visible Realm\" ")
            call.respond(HttpStatusCode.Unauthorized)
            finish()
            return
        }

        val credentials = BasicAuth.parse(authorizationHeader)

        val identityOf = feature.identityService.identityOf(credentials.username)

        var validIdentity = false
        if (identityOf != null) {
            validIdentity = feature.identityService.validIdentity(identityOf, credentials.password)
        }

        if (!validIdentity) {
            call.response.header("WWW-Authenticate", "Basic realm=\"User Visible Realm\" ")
            call.respond(HttpStatusCode.Unauthorized)
            finish()
            return
        }


        // @TODO: Should not be generated here
        val codeToken = CodeToken(
                UUID.randomUUID().toString(),
                3600,
                identityOf!!.username,
                clientOf.clientId,
                queryParameters["redirect_uri"]!!,
                // @TODO: Implement me
                setOf()
        )

        feature.tokenStore.storeCodeToken(codeToken)

        call.respondRedirect(
                queryParameters["redirect_uri"] + "?code=${codeToken.codeToken}"
        )

        finish()
        return
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}

