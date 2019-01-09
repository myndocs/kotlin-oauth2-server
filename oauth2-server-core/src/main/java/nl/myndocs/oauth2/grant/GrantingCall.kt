package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.request.CallContext

interface GrantingCall {
    val callContext: CallContext
    val tokenService: TokenService
}