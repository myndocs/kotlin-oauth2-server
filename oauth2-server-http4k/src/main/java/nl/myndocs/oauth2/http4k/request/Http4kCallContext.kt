package nl.myndocs.oauth2.http4k.request

import nl.myndocs.oauth2.http4k.response.ResponseBuilder
import nl.myndocs.oauth2.json.JsonMapper
import nl.myndocs.oauth2.request.CallContext
import org.http4k.core.Request
import org.http4k.core.body.form
import org.http4k.core.queries

class Http4kCallContext(val request: Request, val responseBuilder: ResponseBuilder) : CallContext {
    override val path: String = request.uri.path
    override val method: String = request.method.name
    override val headers: Map<String, String> = request.headers
            .toMap()
            .filterValues { it != null }
            .map { it.key.toLowerCase() to it.value!! }
            .toMap()
    override val queryParameters: Map<String, String> = request.uri.queries()
            .toMap()
            .filterValues { it != null }
            .mapValues { it.value!! }
            .map { it.key.toLowerCase() to it.value }
            .toMap()

    override val formParameters: Map<String, String> = request.form()
            .toMap()
            .filterValues { it != null }
            .mapValues { it.value!! }
            .map { it.key.toLowerCase() to it.value }
            .toMap()

    override fun respondStatus(statusCode: Int) {
        responseBuilder.statusCode = statusCode
    }

    override fun respondHeader(name: String, value: String) {
        responseBuilder.headers[name] = value
    }

    override fun respondJson(content: Any) {
        responseBuilder.body = JsonMapper.toJson(content)
    }

    override fun redirect(uri: String) {
        responseBuilder.statusCode = 302
        responseBuilder.headers["Location"] = uri
    }
}