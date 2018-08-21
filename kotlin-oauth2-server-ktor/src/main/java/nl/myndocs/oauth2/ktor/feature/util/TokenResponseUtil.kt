package nl.myndocs.oauth2.ktor.feature.util

import nl.myndocs.oauth2.ktor.feature.json.MapToJson
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.token.toMap

fun TokenResponse.toJson() = MapToJson.toJson(this.toMap())!!