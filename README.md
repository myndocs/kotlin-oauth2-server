# Kotlin OAuth2 server 
## Goal
The goal of this project is to provide a simple OAuth2 library which can be implemented in any framework

Configuring the oauth2 server for any framework should be simple and understandable.
It encourages to adapt to existing implementations instead the other way around.

# Frameworks
## Setup
### Maven
First define the version to be used and set it as a property
```xml
<properties>
    <myndocs.oauth.version>0.2.0</myndocs.oauth.version>
</properties>
```

Include the following repository in order to download the artifacts
```xml
<repositories>
    <repository>
        <id>myndocs-oauth2</id>
        <url>https://repo.myndocs.nl/repository/oauth2</url>
    </repository>
</repositories>
```

For the frameworks examples we need at least the following dependencies:
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-core</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>

<!-- In memory dependencies -->
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-client-inmemory</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-identity-inmemory</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-token-store-inmemory</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

## Ktor
The following dependency is required along with the dependencies described in Setup

```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-ktor</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

In memory example for Ktor:
```kotlin
embeddedServer(Netty, 8080) {
    install(Oauth2ServerFeature) {
            tokenService = Oauth2TokenServiceBuilder.build {
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
    }
}.start(wait = true)
```

## Javalin
The following dependency is required along with the dependencies described in Setup
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-javalin</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

In memory example for Javalin:
```kotlin
Javalin.create().apply {
    enableOauthServer {
        tokenService = Oauth2TokenServiceBuilder.build {
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

    }
}.start(7000)
```

## Spark java
The following dependency is required along with the dependencies described in Setup
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-sparkjava</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

In memory example for Spark java:
```kotlin
Oauth2Server.configureOauth2Server {
    tokenService = Oauth2TokenServiceBuilder.build {
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
}
```
## http4k
The following dependency is required along with the dependencies described in Setup
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-http4k</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```

In memory example for http4k:
```kotlin
val app: HttpHandler = routes(
            "/ping" bind GET to { _: Request -> Response(Status.OK).body("pong!") }
    ) `enable oauth2` {
        tokenService = Oauth2TokenServiceBuilder.build {
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
    }

    app.asServer(Jetty(9000)).start()
```

**Note:** `/ping` is only added for demonstration for own defined routes.
# Custom implementation
## Identity service
Users can be authenticate through the identity service. In OAuth2 terms this would be the resource owner.

```kotlin
fun identityOf(forClient: Client, username: String): Identity?

fun validCredentials(forClient: Client, identity: Identity, password: String): Boolean

fun allowedScopes(forClient: Client, identity: Identity, scopes: Set<String>): Set<String>
```

Each of the methods that needs to be implemented contains `Client`. This could give you extra flexibility.
For example you could have user base per client, instead of have users over all clients.

## Client service
Client service is similar to the identity service. 

```kotlin
fun clientOf(clientId: String): Client?

fun validClient(client: Client, clientSecret: String): Boolean
```

## Token store
The following methods have to be implemented for a token store.

```kotlin
fun storeAccessToken(accessToken: AccessToken)

fun accessToken(token: String): AccessToken?

fun revokeAccessToken(token: String)

fun storeCodeToken(codeToken: CodeToken)

fun codeToken(token: String): CodeToken?

fun consumeCodeToken(token: String): CodeToken?

fun storeRefreshToken(refreshToken: RefreshToken)

fun refreshToken(token: String): RefreshToken?

fun revokeRefreshToken(token: String)

```

When `AccessToken` is passed to `storeAccessToken` and it contains a `RefreshToken`, then `storeAccessToken` is also responsible for saving the refresh token.