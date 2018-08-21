package nl.myndocs.oauth2.identity

import nl.myndocs.oauth2.client.Client

data class UserInfo(
        val identity: Identity,
        val client: Client,
        val scopes: Set<String>
)