package nl.myndocs.oauth2.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.CodeChallengeMethod
import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.extension.sha256
import nl.myndocs.oauth2.identity.inmemory.InMemoryIdentity
import nl.myndocs.oauth2.tokenstore.inmemory.InMemoryTokenStore
import okhttp3.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*

abstract class BaseIntegrationTest {
    var localPort: Int? = null
    val configBuilder: ConfigurationBuilder.Configuration.() -> Unit = {
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
                .client {
                    clientId = "testapp_pkce"
                    scopes = setOf("trusted")
                    redirectUris = setOf("http://localhost:8080/callback")
                    authorizedGrantTypes = setOf(
                            AuthorizedGrantType.AUTHORIZATION_CODE
                    )
                    allowedCodeChallengeMethods = setOf(
                            CodeChallengeMethod.S256
                    )
                    public = true
                }
        tokenStore = InMemoryTokenStore()

    }

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    @Disabled
    fun `test password grant flow`() {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("grant_type", "password")
                .add("username", "foo")
                .add("password", "bar")
                .add("client_id", "testapp")
                .add("client_secret", "testpass")
                .build()

        val url = buildOauthTokenUri()

        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        val response = client.newCall(request)
                .execute()

        val values = objectMapper.readMap(response.body()!!.string())

        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        response.close()
    }

    @Test
    @Disabled
    fun `test authorization grant flow`() {

        val client = OkHttpClient.Builder()
                .followRedirects(false)
                .build()

        val url = HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(localPort!!)
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .setQueryParameter("response_type", "code")
                .setQueryParameter("client_id", "testapp")
                .setQueryParameter("redirect_uri", "http://localhost:8080/callback")
                .build()

        val request = Request.Builder()
                .addHeader("Authorization", Credentials.basic("foo", "bar"))
                .url(url)
                .get()
                .build()

        val response = client.newCall(request)
                .execute()

        response.close()

        val body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", response.header("location")!!.asQueryParameters().getValue("code"))
                .add("redirect_uri", "http://localhost:8080/callback")
                .add("client_id", "testapp")
                .add("client_secret", "testpass")
                .build()

        val tokenUrl = buildOauthTokenUri()

        val tokenRequest = Request.Builder()
                .url(tokenUrl)
                .post(body)
                .build()

        val tokenResponse = client.newCall(tokenRequest)
                .execute()

        val values = objectMapper.readMap(tokenResponse.body()!!.string())
        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        tokenResponse.close()
    }

    @Test
    fun `test authorization grant flow with PKCE`() {
        val client = OkHttpClient.Builder()
                .followRedirects(false)
                .build()

        val codeVerifier = "simple_challenge"
        val codeChallenge = codeVerifier.sha256()
        val codeChallengeMethod = CodeChallengeMethod.S256.value

        val url = HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(localPort!!)
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .setQueryParameter("response_type", "code")
                .setQueryParameter("client_id", "testapp_pkce")
                .setQueryParameter("redirect_uri", "http://localhost:8080/callback")
                .setQueryParameter("code_challenge", codeChallenge)
                .setQueryParameter("code_challenge_method", codeChallengeMethod)
                .build()

        val request = Request.Builder()
                .addHeader("Authorization", Credentials.basic("foo", "bar"))
                .url(url)
                .get()
                .build()

        val response = client.newCall(request)
                .execute()

        response.close()

        val body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", response.header("location")!!.asQueryParameters().getValue("code"))
                .add("redirect_uri", "http://localhost:8080/callback")
                .add("client_id", "testapp_pkce")
                .add("code_verifier", codeVerifier)
                .build()

        val tokenUrl = buildOauthTokenUri()

        val tokenRequest = Request.Builder()
                .url(tokenUrl)
                .post(body)
                .build()

        val tokenResponse = client.newCall(tokenRequest)
                .execute()

        val values = objectMapper.readMap(tokenResponse.body()!!.string())
        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        tokenResponse.close()
    }

    @Test
    @Disabled
    fun `test client credentials flow`() {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", "testapp")
                .add("client_secret", "testpass")
                .build()

        val tokenRequest = Request.Builder()
                .url(buildOauthTokenUri())
                .post(body)
                .build()

        val tokenResponse = client.newCall(tokenRequest)
                .execute()

        val values = objectMapper.readMap(tokenResponse.body()!!.string())
        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        tokenResponse.close()

    }

    private fun buildOauthTokenUri() =
            HttpUrl.Builder()
                    .scheme("http")
                    .host("localhost")
                    .port(localPort!!)
                    .addPathSegment("oauth")
                    .addPathSegment("token")
                    .build()
}

fun ObjectMapper.readMap(content: String) = this.readValue(content, Map::class.java)

fun String.asQueryParameters() =
    split("?")[1]
        .split("&")
        .map { it.split("=") }
        .associate { Pair(it[0], it[1]) }
