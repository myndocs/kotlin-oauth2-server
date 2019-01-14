# Spark java

## Dependencies
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-sparkjava</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

## Implementation
```kotlin
Oauth2Server.configureOauth2Server {
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
                redirectUris = setOf("https://localhost:4567/callback")
                authorizedGrantTypes = setOf(
                        AuthorizedGrantType.AUTHORIZATION_CODE,
                        AuthorizedGrantType.PASSWORD,
                        AuthorizedGrantType.IMPLICIT,
                        AuthorizedGrantType.REFRESH_TOKEN
                )
            }
    tokenStore = InMemoryTokenStore()
}
```
