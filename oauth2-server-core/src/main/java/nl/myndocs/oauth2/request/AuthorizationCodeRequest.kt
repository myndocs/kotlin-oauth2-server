package nl.myndocs.oauth2.request

data class AuthorizationCodeRequest(
    override val clientId: String?,
    override val clientSecret: String?,
    val code: String?,
    val redirectUri: String?,
    val codeVerifier: String? = null
) : ClientRequest {
    val grant_type = "authorization_code"
}