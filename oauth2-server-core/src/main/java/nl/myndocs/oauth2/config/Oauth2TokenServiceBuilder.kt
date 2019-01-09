package nl.myndocs.oauth2.config

import nl.myndocs.oauth2.Oauth2TokenService
import nl.myndocs.oauth2.client.ClientService
import nl.myndocs.oauth2.grant.*
import nl.myndocs.oauth2.identity.IdentityService
import nl.myndocs.oauth2.token.TokenStore
import nl.myndocs.oauth2.token.converter.*

object Oauth2TokenServiceBuilder {
    class Configuration {
        var identityService: IdentityService? = null
        var clientService: ClientService? = null
        var tokenStore: TokenStore? = null
        var allowedGrantAuthorizers: Map<String, GrantAuthorizer<*>>? = null
        var additionalGrantAuthorizers: Map<String, GrantAuthorizer<*>>? = null
        var accessTokenConverter: AccessTokenConverter = UUIDAccessTokenConverter()
        var refreshTokenConverter: RefreshTokenConverter = UUIDRefreshTokenConverter()
        var codeTokenConverter: CodeTokenConverter = UUIDCodeTokenConverter()
    }

    fun build(configurer: Configuration.() -> Unit): Oauth2TokenService {
        val configuration = Configuration()
        configurer(configuration)

        val allowedGrantAuthorizers = (
                configuration.allowedGrantAuthorizers ?: mapOf(
                        "password" to PasswordGrantAuthorizer(configuration.clientService!!, configuration.identityService!!),
                        "authorization_code" to AuthorizationCodeGrantAuthorizer(configuration.clientService!!, configuration.identityService!!, configuration.tokenStore!!),
                        "refresh_token" to RefreshTokenGrantAuthorizer(configuration.clientService!!, configuration.identityService!!, configuration.tokenStore!!),
                        "client_credentials" to ClientCredentialsGrantAuthorizer(configuration.clientService!!)
                )
                ).plus(configuration.additionalGrantAuthorizers ?: emptyMap())

        return Oauth2TokenService(
                allowedGrantAuthorizers,
                configuration.identityService!!,
                configuration.clientService!!,
                configuration.tokenStore!!,
                configuration.accessTokenConverter,
                configuration.refreshTokenConverter,
                configuration.codeTokenConverter
        )
    }
}