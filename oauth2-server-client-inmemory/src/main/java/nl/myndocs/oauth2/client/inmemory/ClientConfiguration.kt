package nl.myndocs.oauth2.client.inmemory

import nl.myndocs.oauth2.client.CodeChallengeMethod

data class ClientConfiguration(
        var clientId: String? = null,
        var clientSecret: String? = null,
        var scopes: Set<String> = setOf(),
        var redirectUris: Set<String> = setOf(),
        var authorizedGrantTypes: Set<String> = setOf(),
        var allowedCodeChallengeMethods: Set<CodeChallengeMethod> = emptySet(),
        var forcePKCE: Boolean = false,
        var public: Boolean = false
)