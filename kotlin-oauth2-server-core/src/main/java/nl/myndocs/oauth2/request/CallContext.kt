package nl.myndocs.oauth2.request

interface CallContext<T> {
    /**
     * Driver gives ability to use internal class to do low-level functionality
     * This would normally be the request/call/context object
     */
    val driver: T
    val path: String
    val method: String
    val headers: Map<String, String>
    val queryParameters: Map<String, String>
    val formParameters: Map<String, String>

    fun respondStatus(statusCode: Int)
    fun respondHeader(name: String, value: String)
    fun respondJson(content: Any)
    fun redirect(uri: String)
}