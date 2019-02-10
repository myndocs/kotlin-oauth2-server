package nl.myndocs.convert

import com.auth0.jwt.JWT
import nl.myndocs.oauth2.identity.Identity
import java.time.Instant
import java.util.*

object DefaultJwtBuilder : JwtBuilder {
    override fun buildJwt(identity: Identity?, clientId: String, requestedScopes: Set<String>, expiresInSeconds: Long) =
            JWT.create()
                    .withIssuedAt(Date.from(Instant.now()))
                    .withExpiresAt(
                            Date.from(
                                    Instant.now()
                                            .plusSeconds(expiresInSeconds)
                            )
                    )
                    .withClaim("client_id", clientId)
                    .withArrayClaim("scopes", requestedScopes.toTypedArray())
                    .let { withBuilder -> if (identity != null) withBuilder.withClaim("username", identity.username) else withBuilder }
}