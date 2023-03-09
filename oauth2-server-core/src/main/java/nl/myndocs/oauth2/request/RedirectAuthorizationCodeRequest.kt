package nl.myndocs.oauth2.request

import nl.myndocs.oauth2.client.CodeChallengeMethod

class RedirectAuthorizationCodeRequest(
        val clientId: String?,
        val codeChallenge: String?,
        val codeChallengeMethod: CodeChallengeMethod?,
        val redirectUri: String?,
        val username: String?,
        val password: String?,
        val scope: String?
)