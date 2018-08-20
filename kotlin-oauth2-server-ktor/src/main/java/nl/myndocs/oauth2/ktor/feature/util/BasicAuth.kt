package nl.myndocs.oauth2.ktor.feature.util

import java.util.*

object BasicAuth {
    fun parse(authorization: String): Credentials {
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

        return Credentials(username, password)
    }
}