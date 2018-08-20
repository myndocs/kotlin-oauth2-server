package nl.myndocs.oauth2.exception

class InvalidRequestException(message: String) : OauthException(OauthError.INVALID_REQUEST, message)