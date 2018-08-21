package nl.myndocs.oauth2.ktor.feature.util

import nl.myndocs.oauth2.exception.OauthException
import nl.myndocs.oauth2.exception.toMap
import nl.myndocs.oauth2.ktor.feature.json.MapToJson

fun OauthException.toJson() = MapToJson.toJson(this.toMap())!!