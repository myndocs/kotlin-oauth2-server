package nl.myndocs.oauth2.identity.inmemory

data class IdentityConfiguration(
        var username: String? = null,
        var password: String? = null,
        var scopes: Set<String> = setOf()
)