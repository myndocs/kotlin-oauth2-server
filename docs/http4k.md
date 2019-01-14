# http4k

## Dependencies
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-http4k</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

## Implementation
```kotlin
val app: HttpHandler = routes(
            "/ping" bind GET to { _: Request -> Response(Status.OK).body("pong!") }
    ) `enable oauth2` {
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
                    redirectUris = setOf("http://localhost:8080/callback")
                    authorizedGrantTypes = setOf(
                            AuthorizedGrantType.AUTHORIZATION_CODE,
                            AuthorizedGrantType.PASSWORD,
                            AuthorizedGrantType.IMPLICIT,
                            AuthorizedGrantType.REFRESH_TOKEN
                    )
                }
        tokenStore = InMemoryTokenStore()
    }

    app.asServer(Jetty(9000)).start()
```
