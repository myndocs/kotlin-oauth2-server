package nl.myndocs.oauth2.request

interface ClientRequest {
    val clientId: String
    val clientSecret: String
}
