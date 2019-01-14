package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.exception.*
import nl.myndocs.oauth2.grant.Granter
import nl.myndocs.oauth2.grant.GrantingCall
import nl.myndocs.oauth2.grant.redirect
import nl.myndocs.oauth2.grant.tokenInfo
import nl.myndocs.oauth2.identity.TokenInfo
import nl.myndocs.oauth2.request.CallContext
import nl.myndocs.oauth2.request.RedirectAuthorizationCodeRequest
import nl.myndocs.oauth2.request.RedirectTokenRequest
import nl.myndocs.oauth2.request.headerCaseInsensitive

class CallRouter(
        val tokenEndpoint: String,
        val authorizeEndpoint: String,
        val tokenInfoEndpoint: String,
        private val tokenInfoCallback: (TokenInfo) -> Map<String, Any?>,
        private val granters: List<GrantingCall.() -> Granter>,
        private val grantingCallFactory: (CallContext) -> GrantingCall
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
            tokenInfoEndpoint -> routeTokenInfoEndpoint(callContext)
        }
    }

    private fun routeTokenEndpoint(callContext: CallContext) {
        if (callContext.method.toLowerCase() != METHOD_POST) {
            return
        }

        try {
            val grantType = callContext.formParameters["grant_type"]
                    ?: throw InvalidRequestException("'grant_type' not given")

            val grantingCall = grantingCallFactory(callContext)

            val granterMap = granters
                    .map {
                        val granter = grantingCall.it()
                        granter.grantType to granter
                    }
                    .toMap()

            val allowedGrantTypes = granterMap.keys

            if (!allowedGrantTypes.contains(grantType)) {
                throw InvalidGrantException("'grant_type' with value '$grantType' not allowed")
            }

            granterMap[grantType]!!.callback.invoke()
        } catch (oauthException: OauthException) {
            callContext.respondStatus(STATUS_BAD_REQUEST)
            callContext.respondJson(oauthException.toMap())
        }
    }

    fun routeAuthorizationCodeRedirect(
            callContext: CallContext,
            authorizer: Authorizer
    ) {
        val queryParameters = callContext.queryParameters
        val credentials = authorizer.extractCredentials()
        try {
            val redirect = grantingCallFactory(callContext).redirect(
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
            callContext.respondStatus(STATUS_UNAUTHORIZED)
            authorizer.failedAuthentication()
        }
    }


    fun routeAccessTokenRedirect(
            callContext: CallContext,
            authorizer: Authorizer
    ) {
        val queryParameters = callContext.queryParameters
        val credentials = authorizer.extractCredentials()

        try {
            val redirect = grantingCallFactory(callContext).redirect(
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
                "code" -> routeAuthorizationCodeRedirect(callContext, authorizer)
                "token" -> routeAccessTokenRedirect(callContext, authorizer)
            }
        } catch (oauthException: OauthException) {
            callContext.respondStatus(STATUS_BAD_REQUEST)
            callContext.respondJson(oauthException.toMap())
        }
    }

    private fun routeTokenInfoEndpoint(callContext: CallContext) {
        if (callContext.method.toLowerCase() != METHOD_GET) {
            return
        }

        val authorization = callContext.headerCaseInsensitive("Authorization")

        if (authorization == null || !authorization.startsWith("bearer ", true)) {
            callContext.respondStatus(STATUS_UNAUTHORIZED)
            return
        }

        val token = authorization.substring(7)

        val tokenInfoCallback = tokenInfoCallback(grantingCallFactory(callContext).tokenInfo(token))

        callContext.respondJson(tokenInfoCallback)
    }
}