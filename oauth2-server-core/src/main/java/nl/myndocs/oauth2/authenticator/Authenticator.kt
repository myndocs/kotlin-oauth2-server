package nl.myndocs.oauth2.authenticator

import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.identity.Identity

interface Authenticator {
    fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean
}