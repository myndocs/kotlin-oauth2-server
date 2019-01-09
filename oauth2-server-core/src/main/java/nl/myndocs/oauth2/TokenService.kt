package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.grant.GrantAuthorizer
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken

interface TokenService {
    val allowedGrantAuthorizers: Map<String, GrantAuthorizer<*>>

    fun <TGrantType : ClientRequest> authorize(grantType: String, clientRequest: TGrantType): TokenResponse

    fun redirect(
            redirect: RedirectAuthorizationCodeRequest,
            authenticator: Authenticator?,
            identityScopeVerifier: IdentityScopeVerifier?
    ): CodeToken

    fun redirect(
            redirect: RedirectTokenRequest,
            authenticator: Authenticator?,
            identityScopeVerifier: IdentityScopeVerifier?
    ): AccessToken

    fun tokenInfo(accessToken: String): TokenInfo
}