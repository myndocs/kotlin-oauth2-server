package nl.myndocs.oauth2.grant

fun granter(grantType: String, callback: () -> Unit) = Granter(grantType, callback)