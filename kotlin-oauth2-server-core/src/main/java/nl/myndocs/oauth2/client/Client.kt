package nl.myndocs.oauth2.client

data class Client(
        val clientId: String,
        val secretVerifier: (String) -> Boolean,
        val clientScopes: Set<String>
)