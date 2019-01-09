package nl.myndocs.oauth2.request

interface WritableCallContext {
    fun respondStatus(statusCode: Int)
    fun respondHeader(name: String, value: String)
    fun respondJson(content: Any)
    fun redirect(uri: String)
}
