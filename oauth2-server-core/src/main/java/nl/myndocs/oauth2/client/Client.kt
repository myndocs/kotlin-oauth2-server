package nl.myndocs.oauth2.client

data class Client(
    val clientId: String,
    val clientScopes: Set<String>,
    val redirectUris: Set<String>,
    val authorizedGrantTypes: Set<String>,
    val allowedCodeChallengeMethods: Set<CodeChallengeMethod> = emptySet(),
    val forcePKCE: Boolean = false,
    val public: Boolean = false
)