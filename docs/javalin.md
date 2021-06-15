# Javalin

## Dependencies

### Maven
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-javalin</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

### Gradle
```groovy
implementation "nl.myndocs:oauth2-server-javalin:$myndocs_oauth_version"
```

## Implementation
```kotlin
Javalin.create().apply {
    enableOauthServer {
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
                    redirectUris = setOf("https://localhost:7000/callback")
                    authorizedGrantTypes = setOf(
                            AuthorizedGrantType.AUTHORIZATION_CODE,
                            AuthorizedGrantType.PASSWORD,
                            AuthorizedGrantType.IMPLICIT,
                            AuthorizedGrantType.REFRESH_TOKEN
                    )
                }
        tokenStore = InMemoryTokenStore()
    }
}.start(7000)
```
