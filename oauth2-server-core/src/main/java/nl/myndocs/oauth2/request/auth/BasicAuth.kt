package nl.myndocs.oauth2.request.auth

import java.util.*

object BasicAuth {
    fun parseCredentials(authorization: String): nl.myndocs.oauth2.authenticator.Credentials {
        var username: String? = null
        var password: String? = null

        if (authorization.startsWith("basic ", true)) {
            val basicAuthorizationString = String(Base64.getDecoder().decode(authorization.substring(6)))

            with(basicAuthorizationString.split(":")) {
                if (this.size == 2) {
                    username = this[0]
                    password = this[1]
                }
            }
        }

        return nl.myndocs.oauth2.authenticator.Credentials(username, password)
    }
}