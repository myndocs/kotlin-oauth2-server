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
    <myndocs.oauth.version>0.3.1</myndocs.oauth.version>
</properties>
```

Include the following repository in order to download the artifacts
```xml
<repositories>
    <repository>
        <id>myndocs-oauth2</id>
        <url>https://dl.bintray.com/adhesivee/oauth2-server</url>
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

