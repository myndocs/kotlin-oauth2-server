package nl.myndocs.oauth2.token

data class AccessToken(
        val accessToken: String,
        // @TODO: tokenType is misleading. this is about header Bearer not REFRESH_TOKEN
        val tokenType: String,
        val expiresIn: Int,
        val username: String,
        val clientId: String,
        val scopes: Set<String>,
        val refreshToken: String?
)