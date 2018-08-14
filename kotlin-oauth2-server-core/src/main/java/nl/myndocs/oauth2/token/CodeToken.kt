package nl.myndocs.oauth2.token

data class CodeToken(
        val codeToken: String,
        // @TODO: Is this according spec?
        val expiresIn: Int,
        val username: String,
        val clientId: String,
        val redirectUri: String,
        val scopes: Set<String>
)