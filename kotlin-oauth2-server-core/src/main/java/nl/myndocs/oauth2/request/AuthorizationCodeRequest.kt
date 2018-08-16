package nl.myndocs.oauth2.request

data class AuthorizationCodeRequest(
        val clientId: String,
        val clientSecret: String,
        val code: String,
        val redirectUri: String
) {
    val grant_type = "authorization_code"
}