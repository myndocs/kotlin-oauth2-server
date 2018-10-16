package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.response.TokenResponse
import nl.myndocs.oauth2.scope.ScopeParser
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.CodeToken
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import nl.myndocs.oauth2.token.converter.CodeTokenConverter
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter

class Oauth2TokenService(
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore,
        private val accessTokenConverter: AccessTokenConverter,
        private val refreshTokenConverter: RefreshTokenConverter,
        private val codeTokenConverter: CodeTokenConverter
) : TokenService {
    private val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"
    /**
     * @throws InvalidIdentityException
     * @throws InvalidClientException
     * @throws InvalidScopeException
     */
    override fun authorize(passwordGrantRequest: PasswordGrantRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(passwordGrantRequest)

        if (passwordGrantRequest.username == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
        }

        if (passwordGrantRequest.password == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
        }

        val requestedClient = clientService.clientOf(passwordGrantRequest.clientId!!) ?: throw InvalidClientException()

        val authorizedGrantType = AuthorizedGrantType.PASSWORD
        if (!requestedClient.authorizedGrantTypes.contains(authorizedGrantType)) {
            throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
        }

        val requestedIdentity = identityService.identityOf(
                requestedClient, passwordGrantRequest.username
        )

        if (requestedIdentity == null || !identityService.validCredentials(requestedClient, requestedIdentity, passwordGrantRequest.password)) {
            throw InvalidIdentityException()
        }

        var requestedScopes = ScopeParser.parseScopes(passwordGrantRequest.scope)
                .toSet()

        if (passwordGrantRequest.scope == null) {
            requestedScopes = requestedClient.clientScopes
        }

        validateScopes(requestedClient, requestedIdentity, requestedScopes)

        val accessToken = accessTokenConverter.convertToToken(
                requestedIdentity.username,
                requestedClient.clientId,
                requestedScopes,
                refreshTokenConverter.convertToToken(
                        requestedIdentity.username,
                        requestedClient.clientId,
                        requestedScopes
                )
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    override fun authorize(authorizationCodeRequest: AuthorizationCodeRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(authorizationCodeRequest)

        if (authorizationCodeRequest.code == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("code"))
        }

        if (authorizationCodeRequest.redirectUri == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
        }

        val consumeCodeToken = tokenStore.consumeCodeToken(authorizationCodeRequest.code)
                ?: throw InvalidGrantException()


        if (consumeCodeToken.redirectUri != authorizationCodeRequest.redirectUri || consumeCodeToken.clientId != authorizationCodeRequest.clientId) {
            throw InvalidGrantException()
        }

        val accessToken = accessTokenConverter.convertToToken(
                consumeCodeToken.username,
                consumeCodeToken.clientId,
                consumeCodeToken.scopes,
                refreshTokenConverter.convertToToken(
                        consumeCodeToken.username,
                        consumeCodeToken.clientId,
                        consumeCodeToken.scopes
                )
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    override fun authorize(clientCredentialsRequest: ClientCredentialsRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(clientCredentialsRequest)

        val requestedClient = clientService.clientOf(clientCredentialsRequest.clientId!!) ?: throw InvalidClientException()

        val scopes = clientCredentialsRequest.scope
            ?.let { ScopeParser.parseScopes(it).toSet() }
            ?: requestedClient.clientScopes

        val accessToken = accessTokenConverter.convertToToken(
            username = null,
            clientId = clientCredentialsRequest.clientId,
            requestedScopes = scopes,
            refreshToken = refreshTokenConverter.convertToToken(
                username = null,
                clientId = clientCredentialsRequest.clientId,
                requestedScopes = scopes
            )
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    override fun refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse {
        throwExceptionIfUnverifiedClient(refreshTokenRequest)

        if (refreshTokenRequest.refreshToken == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("refresh_token"))
        }

        val refreshToken = tokenStore.refreshToken(refreshTokenRequest.refreshToken) ?: throw InvalidGrantException()

        if (refreshToken.clientId != refreshTokenRequest.clientId) {
            throw InvalidGrantException()
        }

        val client = clientService.clientOf(refreshToken.clientId) ?: throw InvalidClientException()

        val authorizedGrantType = AuthorizedGrantType.REFRESH_TOKEN
        if (!client.authorizedGrantTypes.contains(authorizedGrantType)) {
            throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
        }

        val accessToken = accessTokenConverter.convertToToken(
                refreshToken.username,
                refreshToken.clientId,
                refreshToken.scopes,
                refreshToken
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken.toTokenResponse()
    }

    override fun redirect(
            redirect: RedirectAuthorizationCodeRequest,
            authenticator: Authenticator?,
            identityScopeVerifier: IdentityScopeVerifier?
    ): CodeToken {
        if (redirect.clientId == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))
        }

        if (redirect.username == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
        }

        if (redirect.password == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
        }
        if (redirect.redirectUri == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
        }

        val clientOf = clientService.clientOf(redirect.clientId) ?: throw InvalidClientException()

        if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
            throw InvalidGrantException("invalid 'redirect_uri'")
        }

        val authorizedGrantType = AuthorizedGrantType.AUTHORIZATION_CODE
        if (!clientOf.authorizedGrantTypes.contains(authorizedGrantType)) {
            throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
        }

        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

        var validIdentity = authenticator?.validCredentials(clientOf, identityOf, redirect.password)
                ?: identityService.validCredentials(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw InvalidIdentityException()
        }

        var requestedScopes = ScopeParser.parseScopes(redirect.scope)

        if (redirect.scope == null) {
            requestedScopes = clientOf.clientScopes
        }

        validateScopes(clientOf, identityOf, requestedScopes, identityScopeVerifier)

        val codeToken = codeTokenConverter.convertToToken(
                identityOf.username,
                clientOf.clientId,
                redirect.redirectUri,
                requestedScopes
        )

        tokenStore.storeCodeToken(codeToken)

        return codeToken
    }

    override fun redirect(
            redirect: RedirectTokenRequest,
            authenticator: Authenticator?,
            identityScopeVerifier: IdentityScopeVerifier?
    ): AccessToken {
        if (redirect.clientId == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))
        }

        if (redirect.username == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("username"))
        }

        if (redirect.password == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("password"))
        }
        if (redirect.redirectUri == null) {
            throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("redirect_uri"))
        }

        val clientOf = clientService.clientOf(redirect.clientId) ?: throw InvalidClientException()

        if (!clientOf.redirectUris.contains(redirect.redirectUri)) {
            throw InvalidGrantException("invalid 'redirect_uri'")
        }

        val authorizedGrantType = AuthorizedGrantType.IMPLICIT
        if (!clientOf.authorizedGrantTypes.contains(authorizedGrantType)) {
            throw InvalidGrantException("Authorize not allowed: '$authorizedGrantType'")
        }

        val identityOf = identityService.identityOf(clientOf, redirect.username) ?: throw InvalidIdentityException()

        var validIdentity = authenticator?.validCredentials(clientOf, identityOf, redirect.password)
                ?: identityService.validCredentials(clientOf, identityOf, redirect.password)

        if (!validIdentity) {
            throw InvalidIdentityException()
        }

        var requestedScopes = ScopeParser.parseScopes(redirect.scope)

        if (redirect.scope == null) {
            requestedScopes = clientOf.clientScopes
        }

        validateScopes(clientOf, identityOf, requestedScopes, identityScopeVerifier)

        val accessToken = accessTokenConverter.convertToToken(
                identityOf.username,
                clientOf.clientId,
                requestedScopes,
                null
        )

        tokenStore.storeAccessToken(accessToken)

        return accessToken
    }

    private fun validateScopes(
            client: Client,
            identity: Identity,
            requestedScopes: Set<String>,
            identityScopeVerifier: IdentityScopeVerifier? = null) {
        val scopesAllowed = scopesAllowed(client.clientScopes, requestedScopes)
        if (!scopesAllowed) {
            throw InvalidScopeException(requestedScopes.minus(client.clientScopes))
        }

        val allowedScopes = identityScopeVerifier?.allowedScopes(client, identity, requestedScopes)
                ?: identityService.allowedScopes(client, identity, requestedScopes)

        val ivalidScopes = requestedScopes.minus(allowedScopes)
        if (ivalidScopes.isNotEmpty()) {
            throw InvalidScopeException(ivalidScopes)
        }
    }

    override fun userInfo(accessToken: String): UserInfo {
        val storedAccessToken = tokenStore.accessToken(accessToken) ?: throw InvalidGrantException()
        val client = clientService.clientOf(storedAccessToken.clientId) ?: throw InvalidClientException()
        val identity = storedAccessToken.username?.let { identityService.identityOf(client, it) }
                ?: throw InvalidIdentityException()

        return UserInfo(
                identity,
                client,
                storedAccessToken.scopes
        )
    }

    private fun throwExceptionIfUnverifiedClient(clientRequest: ClientRequest) {
        val clientId = clientRequest.clientId
                ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_id"))

        val clientSecret = clientRequest.clientSecret
                ?: throw InvalidRequestException(INVALID_REQUEST_FIELD_MESSAGE.format("client_secret"))

        val client = clientService.clientOf(clientId) ?: throw InvalidClientException()

        if (!clientService.validClient(client, clientSecret)) {
            throw InvalidClientException()
        }
    }

    private fun scopesAllowed(clientScopes: Set<String>, requestedScopes: Set<String>): Boolean {
        return clientScopes.containsAll(requestedScopes)
    }

    private fun AccessToken.toTokenResponse() = TokenResponse(
            accessToken,
            tokenType,
            expiresIn(),
            refreshToken?.refreshToken
    )
}