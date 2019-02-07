package nl.myndocs.oauth2.grant

import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.exception.InvalidClientException
import nl.myndocs.oauth2.exception.InvalidGrantException
import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.request.RefreshTokenRequest
import nl.myndocs.oauth2.response.TokenResponse


fun GrantingCall.refresh(refreshTokenRequest: RefreshTokenRequest): TokenResponse {
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

    val accessToken = converters.accessTokenConverter.convertToToken(
            refreshToken.identity,
            refreshToken.clientId,
            refreshToken.scopes,
            converters.refreshTokenConverter.convertToToken(refreshToken)
    )

    tokenStore.storeAccessToken(accessToken)

    return accessToken.toTokenResponse()
}
