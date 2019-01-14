package nl.myndocs.convert

import com.auth0.jwt.JWTCreator

interface JwtBuilder {
    fun buildJwt(username: String?, clientId: String, requestedScopes: Set<String>, expiresInSeconds: Long): JWTCreator.Builder
}