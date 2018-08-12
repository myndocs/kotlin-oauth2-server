package nl.myndocs.oauth2.client

interface ClientService {
    fun clientOf(clientId: String): Client?
}