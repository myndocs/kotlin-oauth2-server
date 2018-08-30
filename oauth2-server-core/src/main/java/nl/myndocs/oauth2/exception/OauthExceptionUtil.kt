package nl.myndocs.oauth2.exception

fun OauthException.toMap(): Map<String, String> {

    val mutableMapOf = mutableMapOf<String, String>(
            "error" to this.error.errorName
    )

    if (this.errorDescription != null) {
        mutableMapOf["error_description"] = this.errorDescription
    }

    return mutableMapOf.toMap()
}