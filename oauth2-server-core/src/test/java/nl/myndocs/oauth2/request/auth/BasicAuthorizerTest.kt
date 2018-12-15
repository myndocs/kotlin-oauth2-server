package nl.myndocs.oauth2.request.auth

import io.mockk.every
import io.mockk.mockk
import nl.myndocs.oauth2.request.CallContext
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class BasicAuthorizerTest {

    @Test
    fun `test authorization head is case insensitive with all uppercase input`() {
        `test authorization head is case insensitive with input`(
                "AUTHORIZATION"
        )
    }

    @Test
    fun `test authorization head is case insensitive with all lowercase input`() {
        `test authorization head is case insensitive with input`(
                "authorization"
        )
    }

    private fun `test authorization head is case insensitive with input`(authorizationKeyName: String) {
        val callContext = mockk<CallContext>()
        val username = "test"
        val password = "test-password"

        val testCredentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())

        every { callContext.headers } returns mapOf(authorizationKeyName to "basic $testCredentials")
        val credentials = BasicAuthorizer(callContext)
                .extractCredentials()

        assertThat(credentials, `is`(notNullValue()))
        assertThat(credentials!!.username, `is`(equalTo(username)))
        assertThat(credentials.password, `is`(equalTo(password)))
    }
}