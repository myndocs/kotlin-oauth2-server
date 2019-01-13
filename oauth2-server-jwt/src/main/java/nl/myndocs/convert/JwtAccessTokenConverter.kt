package nl.myndocs.convert

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import java.time.Instant
import java.util.*

class JwtAccessTokenConverter(
        private val algorithm: Algorithm,
        private val accessTokenExpireInSeconds: Int = 3600,
        private val jwtConfiguration: (JWTCreator.Builder) -> JWTCreator.Builder = { builder -> builder }
) : AccessTokenConverter {
    override fun convertToToken(username: String?, clientId: String, requestedScopes: Set<String>, refreshToken: RefreshToken?): AccessToken {
        val jwtBuilder = JWT.create()
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(
                        Date.from(
                                Instant.now()
                                        .plusSeconds(accessTokenExpireInSeconds.toLong())
                        )
                )
                .withClaim("client_id", clientId)
                .withArrayClaim("scopes", requestedScopes.toTypedArray())
                .let { withBuilder -> if (username != null) withBuilder.withClaim("username", username) else withBuilder }
                .let(jwtConfiguration)

        return AccessToken(
                jwtBuilder.sign(algorithm),
                "bearer",
                Instant.now().plusSeconds(accessTokenExpireInSeconds.toLong()),
                username,
                clientId,
                requestedScopes,
                refreshToken
        )
    }
}