# http4k

## Dependencies

### Maven
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-http4k</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

### Gradle
```groovy
compile "nl.myndocs:oauth2-server-http4k:$myndocs_oauth_version"
```


## Implementation
```kotlin
val app: HttpHandler = routes(
            "/ping" bind GET to { _: Request -> Response(Status.OK).body("pong!") }
    ).enableOauth2 {
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
