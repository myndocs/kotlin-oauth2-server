package nl.myndocs.oauth2.client.inmemory

data class ClientConfiguration(
        var clientId: String? = null,
        var clientSecret: String? = null,
        var scopes: Set<String> = setOf(),
        var redirectUris: Set<String> = setOf(),
        var oauthFlows: Set<String> = setOf()
)