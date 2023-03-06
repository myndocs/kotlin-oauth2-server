package nl.myndocs.oauth2.client

import nl.myndocs.oauth2.exception.InvalidRequestException
import nl.myndocs.oauth2.extension.sha256

enum class CodeChallengeMethod(
        val value: String,
        private val validator: (codeChallenge: String, codeVerifier: String) -> Boolean
) {
    Plain("plain", { cc, cv -> cc == cv }),
    S256("S256", { cc, cv -> cc.trimEnd('=') == cv.sha256() });

    companion object {
        fun parse(value: String): CodeChallengeMethod {
            return values().find { it.value == value }
                    ?: throw InvalidRequestException("Selected code_challenge_method not supported")
        }
    }

    fun validate(codeChallenge: String, codeVerifier: String): Boolean {
        return validator(codeChallenge, codeVerifier)
    }
}
