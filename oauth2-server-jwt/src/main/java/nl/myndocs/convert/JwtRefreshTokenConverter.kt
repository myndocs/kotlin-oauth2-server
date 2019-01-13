package nl.myndocs.convert

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import java.time.Instant
import java.util.*

class JwtRefreshTokenConverter(
        private val algorithm: Algorithm,
        private val refreshTokenExpireInSeconds: Int = 86400,
        private val jwtConfiguration: (JWTCreator.Builder) -> JWTCreator.Builder = { builder -> builder }
) : RefreshTokenConverter {
    override fun convertToToken(username: String?, clientId: String, requestedScopes: Set<String>): RefreshToken {
        val jwtBuilder = JWT.create()
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(
                        Date.from(
                                Instant.now()
                                        .plusSeconds(refreshTokenExpireInSeconds.toLong())
                        )
                )
                .withClaim("client_id", clientId)
                .withArrayClaim("scopes", requestedScopes.toTypedArray())
                .let { withBuilder -> if (username != null) withBuilder.withClaim("username", username) else withBuilder }
                .let(jwtConfiguration)

        return RefreshToken(
                jwtBuilder.sign(algorithm),
                Instant.now().plusSeconds(refreshTokenExpireInSeconds.toLong()),
                username,
                clientId,
                requestedScopes
        )
    }
}