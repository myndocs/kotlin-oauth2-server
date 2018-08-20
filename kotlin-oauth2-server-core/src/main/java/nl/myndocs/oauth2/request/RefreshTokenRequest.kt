package nl.myndocs.oauth2.request

data class RefreshTokenRequest(
        override val clientId: String?,
        override val clientSecret: String?,
        val refreshToken: String?
) : ClientRequest {
    val grant_type = "refresh_token"
}