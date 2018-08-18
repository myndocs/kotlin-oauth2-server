package nl.myndocs.oauth2.exception

class InvalidScopeException(val notAllowedScopes: Set<String>) : OauthException(OauthError.INVALID_SCOPE, "Scopes not allowed: $notAllowedScopes")