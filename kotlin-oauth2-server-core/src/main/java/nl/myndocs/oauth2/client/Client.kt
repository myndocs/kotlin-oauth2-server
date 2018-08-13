package nl.myndocs.oauth2.client

data class Client(
        val clientId: String,
        val clientScopes: Set<String>,
        val redirectUris: Set<String>
)