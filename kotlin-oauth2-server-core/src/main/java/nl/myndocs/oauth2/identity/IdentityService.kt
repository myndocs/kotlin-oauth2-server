package nl.myndocs.oauth2.identity

interface IdentityService {
    /**
     * @throws UnverifiedIdentity
     */
    fun verifiedIdentityOf(username: String, password: String): Identity
}