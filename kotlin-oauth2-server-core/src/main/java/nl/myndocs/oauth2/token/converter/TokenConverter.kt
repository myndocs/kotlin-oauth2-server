package nl.myndocs.oauth2.token.converter

interface TokenConverter<T> {
    fun convertToToken(username: String, clientId: String, requestedScopes: Set<String>): T
}