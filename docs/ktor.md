# Ktor

## Dependencies

### Maven
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-ktor</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

### Gradle
```groovy
implementation "nl.myndocs:oauth2-server-ktor:$myndocs_oauth_version"
```

## Implementation
```kotlin
embeddedServer(Netty, 8080) {
    install(Oauth2ServerFeature) {
            identityService = InMemoryIdentity()
                    .identity {
                        username = "foo"
                        password = "bar"
                    }
            clientService = InMemoryClient()
                    .client {
                        clientId = "testapp"
                        clientSecret = "testpass"
                        scopes = setOf("trusted")
                        redirectUris = setOf("https://localhost:8080/callback")
                        authorizedGrantTypes = setOf(
                                AuthorizedGrantType.AUTHORIZATION_CODE,
                                AuthorizedGrantType.PASSWORD,
                                AuthorizedGrantType.IMPLICIT,
                                AuthorizedGrantType.REFRESH_TOKEN
                        )
                    }
            tokenStore = InMemoryTokenStore()
    }
}.start(wait = true)
```
