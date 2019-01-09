package nl.myndocs.oauth2.grant

class Granter(
    val grantType: String,
    val callback: () -> Unit
)