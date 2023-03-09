package nl.myndocs.oauth2.extension

import java.security.MessageDigest
import java.util.*

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val hashBytes = md.digest(this.toByteArray())
    return Base64.getUrlEncoder().encodeToString(hashBytes).trimEnd('=')
}
