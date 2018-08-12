package nl.myndocs.oauth2.identity

interface IdentityService {
    fun identityOf(username: String): Identity?
}