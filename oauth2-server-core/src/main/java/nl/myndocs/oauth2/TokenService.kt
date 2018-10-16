package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken

interface TokenService {
    fun authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse

    fun authorize(authorizationCodeRequest: AuthorizationCodeRequest): TokenResponse

    fun authorize(clientCredentialsRequest: ClientCredentialsRequest): TokenResponse

    fun refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse

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

    fun userInfo(accessToken: String): UserInfo
}