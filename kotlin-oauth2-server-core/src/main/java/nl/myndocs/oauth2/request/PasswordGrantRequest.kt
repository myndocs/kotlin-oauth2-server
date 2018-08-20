package nl.myndocs.oauth2.request

data class PasswordGrantRequest(
        override val clientId: String?,
        override val clientSecret: String?,
        val username: String?,
        val password: String?,
        val scope: String?
) : ClientRequest {
    val grant_type = "password"
}