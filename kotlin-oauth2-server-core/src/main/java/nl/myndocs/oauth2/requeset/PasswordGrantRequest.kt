package nl.myndocs.oauth2.requeset

data class PasswordGrantRequest(
        val clientId: String,
        val clientSecret: String,
        val username: String,
        val password: String,
        val scope: String?
) {
    val grant_type = "password"
}