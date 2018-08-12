package nl.myndocs.oauth2.identity

data class Identity(
        val username: String,
        val passwordVerifier: (String) -> Boolean,
        val allowedScopes: Set<String>
)