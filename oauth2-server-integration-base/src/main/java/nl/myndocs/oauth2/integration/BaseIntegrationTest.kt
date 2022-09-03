package nl.myndocs.oauth2.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import nl.myndocs.oauth2.client.AuthorizedGrantType
import nl.myndocs.oauth2.client.inmemory.InMemoryClient
import nl.myndocs.oauth2.config.ConfigurationBuilder
import nl.myndocs.oauth2.identity.inmemory.InMemoryIdentity
import nl.myndocs.oauth2.tokenstore.inmemory.InMemoryTokenStore
import okhttp3.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
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
        tokenStore = InMemoryTokenStore()

    }

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
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

        val values = objectMapper.readMap(response.body!!.string())

        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        response.close()
    }

    @Test
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

        val body = response.header("location")!!.asQueryParameters()["code"]?.let {
            FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", it)
                .add("redirect_uri", "http://localhost:8080/callback")
                .add("client_id", "testapp")
                .add("client_secret", "testpass")
                .build()
        }

        val tokenUrl = buildOauthTokenUri()

        val tokenRequest = body?.let {
            Request.Builder()
                .url(tokenUrl)
                .post(it)
                .build()
        }

        val tokenResponse = tokenRequest?.let {
            client.newCall(it)
                .execute()
        }

        val values = objectMapper.readMap(tokenResponse?.body!!.string())
        assertThat(values["access_token"], `is`(notNullValue()))
        assertThat(UUID.fromString(values["access_token"] as String), `is`(instanceOf(UUID::class.java)))

        tokenResponse.close()
    }

    @Test
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

        val values = objectMapper.readMap(tokenResponse.body!!.string())
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
