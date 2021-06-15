package nl.myndocs.oauth2.request

class RedirectAuthorizationCodeRequest(
    val clientId: String?,
    val redirectUri: String?,
    val username: String?,
    val password: String?,
    val scope: String?
)