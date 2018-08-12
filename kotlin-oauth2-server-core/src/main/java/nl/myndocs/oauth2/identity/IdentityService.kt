package nl.myndocs.oauth2.identity

interface IdentityService {
    fun identityOF(username: String): Identity?
}