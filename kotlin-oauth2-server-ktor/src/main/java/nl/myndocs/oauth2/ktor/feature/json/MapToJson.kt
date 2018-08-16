package nl.myndocs.oauth2.ktor.feature.json

import com.google.gson.Gson

object MapToJson {
    private val gson = Gson()

    fun toJson(map: Map<String, Any?>) = gson.toJson(map)
}