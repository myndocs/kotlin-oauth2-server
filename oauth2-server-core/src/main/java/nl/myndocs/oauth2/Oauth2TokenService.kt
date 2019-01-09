package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authenticator
import nl.myndocs.oauth2.authenticator.IdentityScopeVerifier
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.Client
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.grant.GrantAuthorizer
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.identity.TokenInfo
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
        override val allowedGrantAuthorizers: Map<String, GrantAuthorizer<*>>,
        private val identityService: IdentityService,
        private val clientService: ClientService,
        private val tokenStore: TokenStore,
        private val accessTokenConverter: AccessTokenConverter,
        private val refreshTokenConverter: RefreshTokenConverter,
        private val codeTokenConverter: CodeTokenConverter
) : TokenService {
    private val INVALID_REQUEST_FIELD_MESSAGE = "'%s' field is missing"
    /**
     * @throws InvalidGrantException
     * @throws InvalidIdentityException
     * @throws InvalidClientException
     * @throws InvalidScopeException
     * @throws IllegalArgumentException
     */
    override fun <TGrantRequest : ClientRequest> authorize(grantType: String, clientRequest: TGrantRequest): TokenResponse {
        val grantTypeAuthorizer = allowedGrantAuthorizers[grantType] as GrantAuthorizer<TGrantRequest>?
                ?: throw InvalidGrantException("'grant_type' with value '$grantType' not allowed")
        if (!grantTypeAuthorizer.clientRequestClass.isInstance(clientRequest)) {
            throw IllegalArgumentException("'grant_type' of type '$grantType' was mapped to '${grantTypeAuthorizer.clientRequestClass.qualifiedName}' but client request was '${clientRequest.javaClass.name}'")
        }

        if (grantTypeAuthorizer.shouldVerifyUnverifiedClient) {
            throwExceptionIfUnverifiedClient(clientRequest)
        }

        val tokenInfo = grantTypeAuthorizer.authorize(clientRequest)

        if (!tokenInfo.client.authorizedGrantTypes.contains(grantType)) {
            throw InvalidGrantException("Authorize not allowed: '$grantType'")
        }

        if (grantTypeAuthorizer.shouldValidateScopes) {
            validateScopes(tokenInfo.client, tokenInfo.identity!!, tokenInfo.scopes, identityService)
        }

        val accessToken = accessTokenConverter.convertToToken(
                username = tokenInfo.identity?.username,
                clientId = tokenInfo.client.clientId,
                requestedScopes = tokenInfo.scopes,
                refreshToken = refreshTokenConverter.convertToToken(
                        username = tokenInfo.identity?.username,
                        clientId = tokenInfo.client.clientId,
                        requestedScopes = tokenInfo.scopes
                )
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
            identityScopeVerifier: IdentityScopeVerifier? = null
    ) {
        val scopesAllowed = scopesAllowed(client.clientScopes, requestedScopes)
        if (!scopesAllowed) {
            throw InvalidScopeException(requestedScopes.minus(client.clientScopes))
        }

        val allowedScopes = identityScopeVerifier?.allowedScopes(client, identity, requestedScopes)
                ?: identityService.allowedScopes(client, identity, requestedScopes)

        val invalidScopes = requestedScopes.minus(allowedScopes)
        if (invalidScopes.isNotEmpty()) {
            throw InvalidScopeException(invalidScopes)
        }
    }

    override fun tokenInfo(accessToken: String): TokenInfo {
        val storedAccessToken = tokenStore.accessToken(accessToken) ?: throw InvalidGrantException()
        val client = clientService.clientOf(storedAccessToken.clientId) ?: throw InvalidClientException()
        val identity = storedAccessToken.username?.let { identityService.identityOf(client, it) }

        return TokenInfo(
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