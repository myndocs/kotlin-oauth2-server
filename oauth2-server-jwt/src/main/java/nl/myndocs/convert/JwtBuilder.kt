package nl.myndocs.convert

import com.auth0.jwt.JWTCreator
import nl.myndocs.oauth2.identity.Identity

interface JwtBuilder {
    fun buildJwt(identity: Identity?, clientId: String, requestedScopes: Set<String>, expiresInSeconds: Long): JWTCreator.Builder
}