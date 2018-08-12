package nl.myndocs.oauth2.identity

data class Identity(
        val username: String,
        val allowedScopes: Set<String>
)