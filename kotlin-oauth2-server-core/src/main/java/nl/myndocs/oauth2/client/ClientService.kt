package nl.myndocs.oauth2.client

interface ClientService {
    /**
     * @throws UnverifiedClientException
     */
    fun verifiedClientOf(clientId: String, clientSecret: String): Client
}