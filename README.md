# Kotlin OAuth2 server 
## Goal
The goal of this project is to provide an OAuth2 library which can be implemented in multiple frameworks

Configuring the oauth2 server for any framework should be simple and understandable.
## Frameworks
### Ktor
Basic setup for Ktor:
```kotlin
embeddedServer(Netty, 8080) {
    install(Oauth2ServerFeature) {
        identityService = InMemoryIdentity()
                .identity {
                    username = "foo"
                    password = "bar"
                    scopes = setOf("trusted")
                }
        clientService = InMemoryClient()
                .client {
                    clientId = "testapp"
                    clientSecret = "testpass"
                    scopes = setOf("trusted")
                    redirectUris = setOf("https://app.localhost/callback")
                }
        tokenStore = InMemoryTokenStore()
    }
}.start(wait = true)
```