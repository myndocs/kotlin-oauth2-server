package nl.myndocs.oauth2.response

data class PasswordGrantResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val refreshToken: String?
)