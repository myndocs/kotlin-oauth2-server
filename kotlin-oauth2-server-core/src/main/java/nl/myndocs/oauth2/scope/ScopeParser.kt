package nl.myndocs.oauth2.scope

object ScopeParser {
    const val SCOPE_SEPARATOR = " "

    fun parseScopes(scopes: String?): Array<String> {
        if (!scopes.isNullOrBlank()) {
            return scopes!!.split(SCOPE_SEPARATOR)
                    .toTypedArray()
        }

        return arrayOf()
    }
}