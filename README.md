# Kotlin OAuth2 server 
## Goal
The goal of this project is to provide a simple OAuth2 library which can be implemented in any framework

Configuring the oauth2 server for any framework should be simple and understandable.
It encourages to adapt to existing implementations instead the other way around.

# Frameworks
## Setup
### Maven

```xml
<properties>
    <myndocs.oauth.version>0.5.0</myndocs.oauth.version>
</properties>

<dependencies>
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
</dependencies>

<repositories>
    <repository>
        <id>myndocs-oauth2</id>
        <url>https://dl.bintray.com/adhesivee/oauth2-server</url>
    </repository>
</repositories>
```

### Gradle
```groovy
repositories {
    maven { url 'https://dl.bintray.com/adhesivee/oauth2-server' }
}

dependencies {
    compile "nl.myndocs:oauth2-server-core:$myndocs_oauth_version"
    // In memory dependencies
    compile "nl.myndocs:oauth2-server-client-inmemory:$myndocs_oauth_version"
    compile "nl.myndocs:oauth2-server-identity-inmemory:$myndocs_oauth_version"
    compile "nl.myndocs:oauth2-server-token-store-inmemory:$myndocs_oauth_version"
}
```


### Framework implementation
The following frameworks are supported:
- [Ktor](docs/ktor.md)
- [Javalin](docs/javalin.md)
- [http4k](docs/http4k.md)
- [Sparkjava](docs/sparkjava.md)

## Configuration
### Routing
Default endpoints are configured:

| Type | Relative url |
| ----- | ------------- |
| token | /oauth/token |
| authorize | /oauth/authorize |
| token info | /oauth/tokeninfo |

These values can be overridden:
```kotlin
tokenEndpoint = "/custom/token"
authorizationEndpoint = "/custom/authorize"
tokenInfoEndpoint = "/custom/tokeninfo"
```

### In memory 
In memory implementations are provided to easily setup the project.

#### Identity
On the `InMemoryIdentity` identities can be registered. These are normally your users:
```kotlin
identityService = InMemoryIdentity()
    .identity {
        username = "foo-1"
        password = "bar"
    }
    .identity {
        username = "foo-2"
        password = "bar"
    }
```

#### Client
On the `InMemoryClient` clients can be registered:
```kotlin
clientService = InMemoryClient()
    .client {
        clientId = "app1-client"
        clientSecret = "testpass"
        scopes = setOf("admin")
        redirectUris = setOf("https://localhost:8080/callback")
        authorizedGrantTypes = setOf(
                AuthorizedGrantType.AUTHORIZATION_CODE,
                AuthorizedGrantType.PASSWORD,
                AuthorizedGrantType.IMPLICIT,
                AuthorizedGrantType.REFRESH_TOKEN
        )
    }
    .client {
            clientId = "app2-client"
            clientSecret = "testpass"
            scopes = setOf("user")
            redirectUris = setOf("https://localhost:8080/callback")
            authorizedGrantTypes = setOf(
                    AuthorizedGrantType.AUTHORIZATION_CODE
            )
        }
```

#### Token store
The `InMemoryTokenStore` stores all kinds of tokens.
```kotlin
tokenStore = InMemoryTokenStore()
```

### Converters

#### Access token converter
By default `UUIDAccessTokenConverter` is used. With a default time-out of 1 hour. To override the time-out for example to half an hour:
```kotlin
accessTokenConverter = UUIDAccessTokenConverter(1800)
```

To use JWT include the following dependency:
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-jwt</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```
This uses [auth0 jwt](https://github.com/auth0/java-jwt). To configure:
```kotlin
accessTokenConverter = JwtAccessTokenConverter(
        algorithm = Algorithm.HMAC256("test123"), // mandatory
        accessTokenExpireInSeconds = 1800, // optional default 3600
        jwtBuilder = DefaultJwtBuilder // optional uses DefaultJwtBuilder by default
)
```

#### Refresh token converter
By default `UUIDRefreshTokenConverter` is used. With a default time-out of 1 hour. To override the time-out for example to half an hour:
```kotlin
refreshTokenConverter = UUIDRefreshTokenConverter(1800)
```

To use JWT include the following dependency:
```xml
<dependency>
    <groupId>nl.myndocs</groupId>
    <artifactId>oauth2-server-jwt</artifactId>
    <version>${myndocs.oauth.version}</version>
</dependency>
```
This uses [auth0 jwt](https://github.com/auth0/java-jwt). To configure:
```kotlin
refreshTokenConverter = JwtRefreshTokenConverter(
        algorithm = Algorithm.HMAC256("test123"), // mandatory
        refreshTokenExpireInSeconds = 1800, // optional default 86400
        jwtBuilder = DefaultJwtBuilder // optional uses DefaultJwtBuilder by default
)
```
#### Code token converter
By default `UUIDCodeTokenConverter` is used. With a default time-out of 5 minutes. To override the time-out for example 2 minutes:
```kotlin
codeTokenConverter = UUIDCodeTokenConverter(120)
```
