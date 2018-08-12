package nl.myndocs.oauth2.scope

class RequestedScopeNotAllowed(val notAllowedScopes: Set<String>) : Exception("Scopes not allowed: $notAllowedScopes")