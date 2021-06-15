package nl.myndocs.oauth2.client

interface ClientService {
    fun clientOf(clientId: String): Client?
    fun validClient(client: Client, clientSecret: String): Boolean
}