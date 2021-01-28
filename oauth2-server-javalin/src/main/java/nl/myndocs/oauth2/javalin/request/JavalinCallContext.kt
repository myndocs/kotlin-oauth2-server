package nl.myndocs.oauth2.javalin.request

import io.javalin.http.Context
import nl.myndocs.oauth2.request.CallContext

class JavalinCallContext(private val context: Context) : CallContext {
    override val path: String = context.path()
    override val method: String = context.method()
    override val headers: Map<String, String> = context.headerMap()
    override val queryParameters: Map<String, String> = context.queryParamMap()
            .mapValues { context.queryParam(it.key) }
            .filterValues { it != null }
            .mapValues { it.value!! }

    override val formParameters: Map<String, String> = context.formParamMap()
            .mapValues { context.formParam(it.key) }
            .filterValues { it != null }
            .mapValues { it.value!! }

    override fun respondStatus(statusCode: Int) {
        context.status(statusCode)
    }

    override fun respondHeader(name: String, value: String) {
        context.header(name, value)
    }

    override fun respondJson(content: Any) {
        context.json(content)
    }

    override fun redirect(uri: String) {
        context.redirect(uri)
    }
}