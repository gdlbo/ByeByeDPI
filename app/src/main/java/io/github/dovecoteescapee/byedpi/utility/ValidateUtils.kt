package io.github.dovecoteescapee.byedpi.utility

import android.net.InetAddresses
import android.os.Build
import java.util.regex.Pattern

fun checkIp(ip: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        InetAddresses.isNumericAddress(ip)
    } else {
        // This pattern doesn't not support IPv6
        // @Suppress("DEPRECATION")
        // Patterns.IP_ADDRESS.matcher(ip).matches()
        true
    }
}

fun checkNotLocalIp(ip: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        InetAddresses.isNumericAddress(ip) && InetAddresses.parseNumericAddress(ip).let {
            !it.isAnyLocalAddress && !it.isLoopbackAddress
        }
    } else {
        // This pattern doesn't not support IPv6
        // @Suppress("DEPRECATION")
        // Patterns.IP_ADDRESS.matcher(ip).matches()
        true
    }
}

private val DOMAIN_NAME_PATTERN = Pattern.compile(
    "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$",
    Pattern.CASE_INSENSITIVE
)

fun checkDomain(domain: String): Boolean {
    if (domain.isEmpty()) return false
    if (domain.length > 253) return false
    
    // Remove protocol if present for validation
    val cleanDomain = domain.removePrefix("https://").removePrefix("http://").split("/")[0].split(":")[0]
    
    return DOMAIN_NAME_PATTERN.matcher(cleanDomain).matches()
}
