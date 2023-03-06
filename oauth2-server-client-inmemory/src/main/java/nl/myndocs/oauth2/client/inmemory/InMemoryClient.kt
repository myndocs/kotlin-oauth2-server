package nl.myndocs.oauth2.client.inmemory

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService

class InMemoryClient : ClientService {
    private val clients = mutableListOf<ClientConfiguration>()

    fun client(inline: ClientConfiguration.() -> Unit): InMemoryClient {
        val client = ClientConfiguration()
        inline(client)

        clients.add(client)
        return this
    }

    override fun clientOf(clientId: String): Client? {
        return clients.filter { it.clientId == clientId }
                .map { client -> Client(client.clientId!!, client.scopes, client.redirectUris, client.authorizedGrantTypes,
                        client.allowedCodeChallengeMethods, client.forcePKCE, client.public) }
                .firstOrNull()
    }

    override fun validClient(client: Client, clientSecret: String): Boolean {
        return configuredClient(client.clientId)!!.clientSecret == clientSecret
    }

    private fun configuredClient(clientId: String) =
            clients.firstOrNull { it.clientId == clientId }
}