package nl.myndocs.oauth2.token

data class RefreshToken(
        val refreshToken: String,
        val expiresIn: Int,
        val username: String,
        val clientId: String,
        val scopes: Set<String>
)