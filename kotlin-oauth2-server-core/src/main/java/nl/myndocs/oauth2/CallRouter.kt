package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.identity.UserInfo
import nl.myndocs.oauth2.request.*
import nl.myndocs.oauth2.token.toMap

class CallRouter(
        private val tokenService: TokenService,
        private val tokenEndpoint: String,
        private val authorizeEndpoint: String,
        private val userinfoEndpoint: String,
        private val userInfoCallback: (UserInfo) -> Map<String, Any?>
) {
    companion object {
        const val METHOD_POST = "post"
        const val METHOD_GET = "get"

        const val STATUS_BAD_REQUEST = 400
        const val STATUS_UNAUTHORIZED = 401

    }

    fun route(
            callContext: CallContext,
            authorizer: Authorizer) {
        when (callContext.path) {
            tokenEndpoint -> routeTokenEndpoint(callContext)
            authorizeEndpoint -> routeAuthorizeEndpoint(callContext, authorizer)
            userinfoEndpoint -> routeUserInfoEndpoint(callContext)
        }
    }

    private fun routeTokenEndpoint(callContext: CallContext) {
        if (callContext.method.toLowerCase() != METHOD_POST) {
            return
        }

        try {
            val allowedGrantTypes = setOf("password", "authorization_code", "refresh_token")
            val grantType = callContext.formParameters["grant_type"]
                    ?: throw InvalidRequestException("'grant_type' not given")

            if (!allowedGrantTypes.contains(grantType)) {
                throw InvalidGrantException("'grant_type' with value '$grantType' not allowed")
            }

            when (grantType) {
                "password" -> routePasswordGrant(callContext, tokenService)
                "authorization_code" -> routeAuthorizationCodeGrant(callContext, tokenService)
                "refresh_token" -> routeRefreshTokenGrant(callContext, tokenService)
            }
        } catch (oauthException: OauthException) {
            callContext.respondStatus(STATUS_BAD_REQUEST)
            callContext.respondJson(oauthException.toMap())
        }
    }

    fun routePasswordGrant(callContext: CallContext, tokenService: TokenService) {
        val tokenResponse = tokenService.authorize(
                PasswordGrantRequest(
                        callContext.formParameters["client_id"],
                        callContext.formParameters["client_secret"],
                        callContext.formParameters["username"],
                        callContext.formParameters["password"],
                        callContext.formParameters["scope"]
                )
        )

        callContext.respondJson(tokenResponse.toMap())
    }

    fun routeRefreshTokenGrant(callContext: CallContext, tokenService: TokenService) {
        val accessToken = tokenService.refresh(
                RefreshTokenRequest(
                        callContext.formParameters["client_id"],
                        callContext.formParameters["client_secret"],
                        callContext.formParameters["refresh_token"]
                )
        )

        callContext.respondJson(accessToken.toMap())
    }

    fun routeAuthorizationCodeGrant(callContext: CallContext, tokenService: TokenService) {
        val accessToken = tokenService.authorize(
                AuthorizationCodeRequest(
                        callContext.formParameters["client_id"],
                        callContext.formParameters["client_secret"],
                        callContext.formParameters["code"],
                        callContext.formParameters["redirect_uri"]
                )
        )

        callContext.respondJson(accessToken.toMap())
    }


    fun routeAuthorizationCodeRedirect(
            callContext: CallContext,
            tokenService: TokenService,
            authorizer: Authorizer
    ) {
        val queryParameters = callContext.queryParameters
        val credentials = authorizer.extractCredentials()
        try {
            val redirect = tokenService.redirect(
                    RedirectAuthorizationCodeRequest(
                            queryParameters["client_id"],
                            queryParameters["redirect_uri"],
                            credentials?.username ?: "",
                            credentials?.password ?: "",
                            queryParameters["scope"]
                    ),
                    authorizer.authenticator(),
                    authorizer.scopesVerifier()
            )

            var stateQueryParameter = ""

            if (queryParameters["state"] != null) {
                stateQueryParameter = "&state=" + queryParameters["state"]
            }

            callContext.redirect(queryParameters["redirect_uri"] + "?code=${redirect.codeToken}$stateQueryParameter")
        } catch (unverifiedIdentityException: InvalidIdentityException) {
            authorizer.failedAuthentication()
            callContext.respondStatus(STATUS_UNAUTHORIZED)
        }
    }


    fun routeAccessTokenRedirect(
            callContext: CallContext,
            tokenService: TokenService,
            authorizer: Authorizer
    ) {
        val queryParameters = callContext.queryParameters
        val credentials = authorizer.extractCredentials()

        try {
            val redirect = tokenService.redirect(
                    RedirectTokenRequest(
                            queryParameters["client_id"],
                            queryParameters["redirect_uri"],
                            credentials?.username ?: "",
                            credentials?.password ?: "",
                            queryParameters["scope"]
                    ),
                    authorizer.authenticator(),
                    authorizer.scopesVerifier()
            )

            var stateQueryParameter = ""

            if (queryParameters["state"] != null) {
                stateQueryParameter = "&state=" + queryParameters["state"]
            }

            callContext.redirect(
                    queryParameters["redirect_uri"] + "#access_token=${redirect.accessToken}" +
                            "&token_type=bearer&expires_in=${redirect.expiresIn()}$stateQueryParameter"
            )

        } catch (unverifiedIdentityException: InvalidIdentityException) {
            authorizer.failedAuthentication()
            callContext.respondStatus(STATUS_UNAUTHORIZED)
        }
    }

    private fun routeAuthorizeEndpoint(callContext: CallContext, authorizer: Authorizer) {
        try {
            if (callContext.method.toLowerCase() != METHOD_GET) {
                return
            }

            val allowedResponseTypes = setOf("code", "token")
            val responseType = callContext.queryParameters["response_type"]
                    ?: throw InvalidRequestException("'response_type' not given")

            if (!allowedResponseTypes.contains(responseType)) {
                throw InvalidGrantException("'grant_type' with value '$responseType' not allowed")
            }

            when (responseType) {
                "code" -> routeAuthorizationCodeRedirect(callContext, tokenService, authorizer)
                "token" -> routeAccessTokenRedirect(callContext, tokenService, authorizer)
            }
        } catch (oauthException: OauthException) {
            callContext.respondStatus(STATUS_BAD_REQUEST)
            callContext.respondJson(oauthException.toMap())
        }
    }

    private fun routeUserInfoEndpoint(callContext: CallContext) {
        if (callContext.method.toLowerCase() != METHOD_GET) {
            return
        }

        val authorization = callContext.headers["Authorization"]

        if (authorization == null || !authorization.startsWith("bearer ", true)) {
            callContext.respondStatus(STATUS_UNAUTHORIZED)
            return
        }

        val token = authorization.substring(7)

        val userInfoCallback = userInfoCallback(tokenService.userInfo(token))

        callContext.respondJson(userInfoCallback)
    }
}