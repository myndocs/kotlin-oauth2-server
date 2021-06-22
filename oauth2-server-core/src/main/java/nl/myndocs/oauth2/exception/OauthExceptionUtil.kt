package nl.myndocs.oauth2.exception

fun OauthException.toMap(): Map<String, String> = with(mutableMapOf("error" to error.errorName)) {
    if (errorDescription != null) {
        this["error_description"] = errorDescription
    }
    toMap()
}