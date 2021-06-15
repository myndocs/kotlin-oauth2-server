package nl.myndocs.oauth2.token.converter

data class Converters(
    val accessTokenConverter: AccessTokenConverter,
    val refreshTokenConverter: RefreshTokenConverter,
    val codeTokenConverter: CodeTokenConverter
)