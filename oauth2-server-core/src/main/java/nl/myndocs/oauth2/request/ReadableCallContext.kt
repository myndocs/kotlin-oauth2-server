package nl.myndocs.oauth2.request

interface ReadableCallContext {
    val path: String
    val method: String
    val headers: Map<String, String>
    val queryParameters: Map<String, String>
    val formParameters: Map<String, String>
}
