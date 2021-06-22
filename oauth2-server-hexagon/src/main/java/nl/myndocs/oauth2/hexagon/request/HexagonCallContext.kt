package nl.myndocs.oauth2.hexagon.request

import com.hexagonkt.http.server.Call
import nl.myndocs.oauth2.json.JsonMapper
import nl.myndocs.oauth2.request.CallContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class HexagonCallContext(val call: Call) : CallContext {
    override val path: String = call.request.path
    override val method: String = call.request.method.name
    override val headers: Map<String, String> = call.request.headers
            .mapValues { it.value.joinToString(";") }

    override val queryParameters: Map<String, String> = (call.request
        .runCatching { queryString }
        .getOrNull() ?: "")
        .split("&")
        .filter { it.contains("=") }
        .associate {
            val (key, value) = it.split("=")
            Pair(
                key.toLowerCase(),
                URLDecoder.decode(value, StandardCharsets.UTF_8.name())
            )
        }

    override val formParameters: Map<String, String> = call.parameters
            .mapValues { it.value.lastOrNull() }
            .filterValues { it != null }
            .mapValues { it.value!! }

    override fun respondStatus(statusCode: Int) {
        call.response.status = statusCode
    }

    override fun respondHeader(name: String, value: String) {
        call.response.setHeader(name, value)
    }

    override fun respondJson(content: Any) {
        call.response.body = JsonMapper.toJson(content)
    }

    override fun redirect(uri: String) {
        call.redirect(uri)
    }
}