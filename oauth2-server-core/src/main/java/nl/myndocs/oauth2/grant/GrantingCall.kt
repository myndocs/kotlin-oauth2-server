package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.response.AccessTokenResponder
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.Converters

interface GrantingCall {
    val callContext: CallContext
    val identityService: IdentityService
    val clientService: ClientService
    val tokenStore: TokenStore
    val converters: Converters
    val accessTokenResponder: AccessTokenResponder
}