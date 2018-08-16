package nl.myndocs.oauth2.ktor.feature.util

import nl.myndocs.oauth2.ktor.feature.json.MapToJson
import nl.myndocs.oauth2.response.TokenResponse

fun TokenResponse.toMap() = mapOf(
        "access_token" to this.accessToken,
        "token_type" to this.tokenType,
        "expires_in" to this.expiresIn,
        "refresh_token" to this.refreshToken
)

fun TokenResponse.toJson() = MapToJson.toJson(toMap())!!