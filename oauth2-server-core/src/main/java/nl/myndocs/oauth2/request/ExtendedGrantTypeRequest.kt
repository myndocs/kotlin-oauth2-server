package nl.myndocs.oauth2.request

class ExtendedGrantTypeRequest(
    val grant_type: String,
    override val clientId: String?,
    override val clientSecret: String?,
    val scope: String?,
    val callContext: ReadableCallContext
) : ClientRequest
