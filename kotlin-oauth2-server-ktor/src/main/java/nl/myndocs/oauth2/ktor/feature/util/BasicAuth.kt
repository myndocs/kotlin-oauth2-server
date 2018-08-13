package nl.myndocs.oauth2.ktor.feature.util

import java.util.*

object BasicAuth {
    fun parse(authorization: String): Credentials {
        authorization.startsWith("basic ", true)

        val basicAuthorizationString = String(
                Base64.getDecoder()
                        .decode(authorization.substring(6))
        )

        val (username, password) = basicAuthorizationString.split(":")

        return Credentials(username, password)
    }
}