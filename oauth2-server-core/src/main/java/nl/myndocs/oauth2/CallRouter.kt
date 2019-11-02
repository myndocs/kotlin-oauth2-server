package nl.myndocs.oauth2

import nl.myndocs.oauth2.authenticator.Credentials
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
import nl.myndocs.oauth2.router.RedirectRouter
import nl.myndocs.oauth2.router.RedirectRouterResponse

class CallRouter(
        val tokenEndpoint: String,
        val authorizeEndpoint: String,
        val tokenInfoEndpoint: String,
        private val tokenInfoCallback: (TokenInfo) -> Map<String, Any?>,
        private val granters: List<GrantingCall.() -> Granter>,
        private val grantingCallFactory: (CallContext) -> GrantingCall
) : RedirectRouter {
    companion object {
        const val METHOD_POST = "post"
        const val METHOD_GET = "get"

        const val STATUS_BAD_REQUEST = 400
        const val STATUS_UNAUTHORIZED = 401

    }

    fun route(callContext: CallContext) {
        when (callContext.path) {
            tokenEndpoint -> routeTokenEndpoint(callContext)
            tokenInfoEndpoint -> routeTokenInfoEndpoint(callContext)
        }
    }

    override fun route(callContext: CallContext, credentials: Credentials?): RedirectRouterResponse {
        return when (callContext.path) {
            authorizeEndpoint -> routeAuthorizeEndpoint(callContext, credentials)
            else -> throw NoRoutesFoundException("Route '${callContext.path}' not found")
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
            credentials: Credentials?
    ): RedirectRouterResponse {
        val queryParameters = callContext.queryParameters
        try {
            val redirect = grantingCallFactory(callContext).redirect(
                    RedirectAuthorizationCodeRequest(
                            queryParameters["client_id"],
                            queryParameters["redirect_uri"],
                            credentials?.username,
                            credentials?.password,
                            queryParameters["scope"]
                    )
            )

            var stateQueryParameter = ""

            if (queryParameters["state"] != null) {
                stateQueryParameter = "&state=" + queryParameters["state"]
            }

            callContext.redirect(queryParameters["redirect_uri"] + "?code=${redirect.codeToken}$stateQueryParameter")

            return RedirectRouterResponse(true)
        } catch (unverifiedIdentityException: InvalidIdentityException) {
            callContext.respondStatus(STATUS_UNAUTHORIZED)

            return RedirectRouterResponse(false)
        }
    }


    fun routeAccessTokenRedirect(
            callContext: CallContext,
            credentials: Credentials?
    ): RedirectRouterResponse {
        val queryParameters = callContext.queryParameters

        try {
            val redirect = grantingCallFactory(callContext).redirect(
                    RedirectTokenRequest(
                            queryParameters["client_id"],
                            queryParameters["redirect_uri"],
                            credentials?.username,
                            credentials?.password,
                            queryParameters["scope"]
                    )
            )

            var stateQueryParameter = ""

            if (queryParameters["state"] != null) {
                stateQueryParameter = "&state=" + queryParameters["state"]
            }

            callContext.redirect(
                    queryParameters["redirect_uri"] + "#access_token=${redirect.accessToken}" +
                            "&token_type=bearer&expires_in=${redirect.expiresIn()}$stateQueryParameter"
            )

            return RedirectRouterResponse(true)
        } catch (unverifiedIdentityException: InvalidIdentityException) {
            callContext.respondStatus(STATUS_UNAUTHORIZED)

            return RedirectRouterResponse(false)
        }
    }

    private fun routeAuthorizeEndpoint(callContext: CallContext, credentials: Credentials?): RedirectRouterResponse {
        try {
            if (!arrayOf(METHOD_GET, METHOD_POST).contains(callContext.method.toLowerCase())) {
                return RedirectRouterResponse(false)
            }

            val responseType = callContext.queryParameters["response_type"]
                    ?: throw InvalidRequestException("'response_type' not given")

            return when (responseType) {
                "code" -> routeAuthorizationCodeRedirect(callContext, credentials)
                "token" -> routeAccessTokenRedirect(callContext, credentials)
                else -> throw InvalidGrantException("'grant_type' with value '$responseType' not allowed")
            }
        } catch (invalidIdentityException: InvalidIdentityException) {
            callContext.respondStatus(STATUS_UNAUTHORIZED)
            callContext.respondJson(invalidIdentityException.toMap())
            return RedirectRouterResponse(false)
        } catch (oauthException: OauthException) {
            callContext.respondStatus(STATUS_BAD_REQUEST)
            callContext.respondJson(oauthException.toMap())

            return RedirectRouterResponse(false)
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