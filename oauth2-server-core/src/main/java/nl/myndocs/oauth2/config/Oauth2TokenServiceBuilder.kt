package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.Oauth2TokenService
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

object Oauth2TokenServiceBuilder {
    class Configuration {
        var identityService: IdentityService? = null
        var clientService: ClientService? = null
        var tokenStore: TokenStore? = null
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter()
        var refreshTokenConverter: RefreshTokenConverter = UUIDRefreshTokenConverter()
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter()
    }

    fun build(configurer: Configuration.() -> Unit): Oauth2TokenService {
        val configuration = Configuration()
        configurer(configuration)

        return Oauth2TokenService(
                configuration.identityService!!,
                configuration.clientService!!,
                configuration.tokenStore!!,
                configuration.accessTokenConverter,
                configuration.refreshTokenConverter,
                configuration.codeTokenConverter
        )
    }
}