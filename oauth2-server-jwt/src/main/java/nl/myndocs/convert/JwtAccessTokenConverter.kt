package nl.myndocs.convert

import com.auth0.jwt.algorithms.Algorithm
import nl.myndocs.oauth2.identity.Identity
import nl.myndocs.oauth2.token.AccessToken
import nl.myndocs.oauth2.token.RefreshToken
import nl.myndocs.oauth2.token.converter.AccessTokenConverter
import java.time.Instant

class JwtAccessTokenConverter(
        private val algorithm: Algorithm,
        private val accessTokenExpireInSeconds: Int = 3600,
        private val jwtBuilder: JwtBuilder = DefaultJwtBuilder
) : AccessTokenConverter {
    override fun convertToToken(identity: Identity?, clientId: String, requestedScopes: Set<String>, refreshToken: RefreshToken?): AccessToken {
        val jwtBuilder = jwtBuilder.buildJwt(
                identity,
                clientId,
                requestedScopes,
                accessTokenExpireInSeconds.toLong()
        )

        return AccessToken(
                jwtBuilder.sign(algorithm),
                "bearer",
                Instant.now().plusSeconds(accessTokenExpireInSeconds.toLong()),
                identity,
                clientId,
                requestedScopes,
                refreshToken
        )
    }
}