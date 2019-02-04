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

        val url = HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(localPort!!)
                .addPathSegment("oauth")
                .addPathSegment("token")
                .build()

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
                .add("code", response.header("location")!!.asQueryParameters()["code"])
                .add("redirect_uri", "http://localhost:8080/callback")
                .add("client_id", "testapp")
                .add("client_secret", "testpass")
                .build()

        val tokenUrl = HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(localPort!!)
                .addPathSegment("oauth")
                .addPathSegment("token")
                .build()

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
}

fun ObjectMapper.readMap(content: String) = this.readValue(content, Map::class.java)

fun String.asQueryParameters() =
        split("?")[1]
                .let { it.split("&") }
                .map { it.split("=") }
                .map { Pair(it[0], it[1]) }
                .toMap()
