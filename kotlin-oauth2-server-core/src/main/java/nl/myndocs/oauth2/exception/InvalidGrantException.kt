package nl.myndocs.oauth2.exception

open class InvalidGrantException(message: String? = null) : OauthException(OauthError.INVALID_GRANT, message)