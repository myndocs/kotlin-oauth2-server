package nl.myndocs.oauth2.scope

object ScopeParser {
    private const val SCOPE_SEPARATOR = " "

    fun parseScopes(scopes: String?): Set<String> {
        if (!scopes.isNullOrBlank()) {
            return scopes.split(SCOPE_SEPARATOR).toSet()
        }

        return setOf()
    }
}