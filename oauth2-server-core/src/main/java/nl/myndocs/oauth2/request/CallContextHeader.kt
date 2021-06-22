package nl.myndocs.oauth2.request

fun CallContext.headerCaseInsensitive(key: String) = headers
    .filter { it.key.equals(key, true) }
    .values
    .firstOrNull()