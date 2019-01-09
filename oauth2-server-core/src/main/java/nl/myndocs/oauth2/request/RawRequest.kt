package nl.myndocs.oauth2.request

data class RawRequest(
    val callContext: CallContext,
    override val clientId: String?,
    override val clientSecret: String?
) : ClientRequest