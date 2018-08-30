package nl.myndocs.oauth2.sparkjava.request

import nl.myndocs.oauth2.sparkjava.json.JsonMapper
import spark.Request
import spark.Response

class SparkjavaCallContext(val request: Request, val response: Response) : CallContext {
    override val path: String = request.pathInfo()
    override val method: String = request.requestMethod()
    override val headers: Map<String, String> = request.headers()
            .map { it.toLowerCase() to request.headers(it) }
            .toMap()

    override val queryParameters: Map<String, String> = request.queryParams()
            .map { it.toLowerCase() to request.queryParams(it) }
            .toMap()

    override val formParameters: Map<String, String> = queryParameters

    override fun respondStatus(statusCode: Int) {
        response.status(statusCode)
        response.body("")
    }

    override fun respondHeader(name: String, value: String) {
        response.header(name, value)
    }

    override fun respondJson(content: Any) {
        response.body(JsonMapper.toJson(content))
    }

    override fun redirect(uri: String) {
        response.redirect(uri)
    }
}