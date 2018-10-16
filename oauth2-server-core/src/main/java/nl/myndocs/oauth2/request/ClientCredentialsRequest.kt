package nl.myndocs.oauth2.request

data class ClientCredentialsRequest(
    override val clientId: String?,
    override val clientSecret: String?,
    val scope: String?
) : ClientRequest{
    val grant_type = "client_credentials"
}