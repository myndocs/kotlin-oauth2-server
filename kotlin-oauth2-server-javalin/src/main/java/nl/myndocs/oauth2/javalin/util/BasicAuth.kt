package nl.myndocs.oauth2.ktor.feature.util

import nl.myndocs.oauth2.javalin.util.Credentials
import java.util.*

object BasicAuth {
    @Deprecated("Removed in 0.2.0")
    fun parse(authorization: String): Credentials {
        val parseCredentials = parseCredentials(authorization)
        return Credentials(parseCredentials.username, parseCredentials.password)
    }

    fun parseCredentials(authorization: String): nl.myndocs.oauth2.authenticator.Credentials {
        var username: String? = null
        var password: String? = null

        if (authorization.startsWith("basic ", true)) {

            val basicAuthorizationString = String(
                    Base64.getDecoder()
                            .decode(authorization.substring(6))
            )

            val splittedString = basicAuthorizationString.split(":")

            if (splittedString.size == 2) {
                username = splittedString[0]
                password = splittedString[1]
            }
        }

        return nl.myndocs.oauth2.authenticator.Credentials(username, password)
    }
}