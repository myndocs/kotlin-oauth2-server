package nl.myndocs.convert

import com.auth0.jwt.algorithms.Algorithm
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.RefreshTokenConverter
import java.time.Instant

class JwtRefreshTokenConverter(
        private val algorithm: Algorithm,
        private val refreshTokenExpireInSeconds: Int = 86400,
        private val jwtBuilder: JwtBuilder = DefaultJwtBuilder
) : RefreshTokenConverter {
    override fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>): RefreshToken {
        val jwtBuilder = jwtBuilder.buildJwt(
                identity,
                clientId,
                requestedScopes,
                refreshTokenExpireInSeconds.toLong()
        )

        return RefreshToken(
                jwtBuilder.sign(algorithm),
                Instant.now().plusSeconds(refreshTokenExpireInSeconds.toLong()),
                identity,
                clientId,
                requestedScopes
        )
    }
}