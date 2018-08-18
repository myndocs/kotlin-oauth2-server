package nl.myndocs.oauth2.ktor.feature.util

import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.ktor.feature.json.MapToJson

fun OauthException.toMap(): Map<String, String> {

    val mutableMapOf = mutableMapOf<String, String>(
            "error" to this.error.errorName
    )

    if (this.errorDescription != null) {
        mutableMapOf["error_description"] = this.errorDescription!!
    }

    return mutableMapOf.toMap()
}

fun OauthException.toJson() = MapToJson.toJson(toMap())!!