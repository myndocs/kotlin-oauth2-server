package nl.myndocs.oauth2.response

import nl.myndocs.oauth2.token.AccessToken

interface AccessTokenResponder {
    fun createResponse(accessToken: AccessToken): Map<String, Any?>
}