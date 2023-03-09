package nl.myndocs.oauth2.request

import nl.myndocs.oauth2.client.CodeChallengeMethod
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class CodeChallengeMethodTest {
    @Test
    fun validatePlain() {
        val codeChallenge = "plain_test"
        val resultValidVerifier = CodeChallengeMethod.Plain.validate(codeChallenge, "plain_test")
        assertThat(resultValidVerifier, `is`(true))

        val resultInvalidVerifier = CodeChallengeMethod.Plain.validate(codeChallenge, "plain_tes")
        assertThat(resultInvalidVerifier, `is`(false))
    }

    @Test
    fun validateS256() {
        val codeChallenge = "W6YWc_4yHwYN-cGDgGmOMHF3l7KDy7VcRjf7q2FVF-o="
        val resultValidVerifier = CodeChallengeMethod.S256.validate(codeChallenge, "s256test")
        assertThat(resultValidVerifier, `is`(true))

        val resultInvalidVerifier = CodeChallengeMethod.S256.validate(codeChallenge, "s256tes")
        assertThat(resultInvalidVerifier, `is`(false))
    }

    @Test
    fun validateS256NoPadding() {
        val codeChallenge = "W6YWc_4yHwYN-cGDgGmOMHF3l7KDy7VcRjf7q2FVF-o"
        val resultValidVerifier = CodeChallengeMethod.S256.validate(codeChallenge, "s256test")
        assertThat(resultValidVerifier, `is`(true))

        val resultInvalidVerifier = CodeChallengeMethod.S256.validate(codeChallenge, "s256tes")
        assertThat(resultInvalidVerifier, `is`(false))
    }
}
