package nl.myndocs.oauth2.http4k.response

import org.http4k.core.Response
import org.http4k.core.Status

class ResponseBuilder(
        var statusCode: Int = 200,
        var headers: MutableMap<String, String> = mutableMapOf(),
        var body: String = ""
) {

    fun build(): Response {
        var response = Response(
                Status(statusCode, "")
        )

        for (header in headers) {
            response = response.header(header.key, header.value)
        }

        response = response.body(body)

        return response
    }
}