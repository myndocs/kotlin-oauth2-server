package nl.myndocs.oauth2.identity.inmemory

import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService

class InMemoryIdentity : IdentityService {
    private val identities = mutableListOf<IdentityConfiguration>()

    fun identity(inline: IdentityConfiguration.() -> Unit): InMemoryIdentity {
        val client = IdentityConfiguration()
        inline(client)

        identities.add(client)
        return this
    }

    override fun identityOf(username: String): Identity? {
        val findConfiguration = findConfiguration(username)

        if (findConfiguration == null) {
            return null
        }

        return Identity(
                findConfiguration.username!!,
                findConfiguration.scopes
        )
    }

    override fun validIdentity(identity: Identity, password: String): Boolean =
            findConfiguration(identity.username)!!.password == password


    private fun findConfiguration(username: String) = identities.firstOrNull { it.username == username }
}