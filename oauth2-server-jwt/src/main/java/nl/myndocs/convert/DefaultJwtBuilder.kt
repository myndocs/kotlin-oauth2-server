package nl.myndocs.convert

import com.auth0.jwt.JWT
import java.time.Instant
import java.util.*

object DefaultJwtBuilder : JwtBuilder {
    override fun buildJwt(username: String?, clientId: String, requestedScopes: Set<String>, expiresInSeconds: Long) =
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
                    .let { withBuilder -> if (username != null) withBuilder.withClaim("username", username) else withBuilder }
}