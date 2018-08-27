package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.CallRouter
import nl.myndocs.oauth2.TokenService
import nl.myndocs.oauth2.authenticator.Authorizer
import nl.myndocs.oauth2.request.CallContext

data class Configuration<T : TokenService>(
        val tokenService: T,
        val callRouter: CallRouter,
        val authorizerFactory: (CallContext) -> Authorizer
)