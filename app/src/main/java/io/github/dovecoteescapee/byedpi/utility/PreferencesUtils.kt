package io.github.dovecoteescapee.byedpi.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.data.Command
import com.google.gson.Gson

fun Context.getPreferences(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.getIntStringNotNull(key: String, defValue: Int): Int =
    getString(key, defValue.toString())?.toIntOrNull() ?: defValue

fun SharedPreferences.getLongStringNotNull(key: String, defValue: Long): Long =
    getString(key, defValue.toString())?.toLongOrNull() ?: defValue

fun SharedPreferences.getStringNotNull(key: String, defValue: String): String =
    getString(key, defValue) ?: defValue

fun SharedPreferences.mode(): Mode =
    Mode.fromString(getStringNotNull("byedpi_mode", "vpn"))

fun SharedPreferences.getSelectedApps(): List<String> {
    return getStringSet("selected_apps", emptySet())?.toList() ?: emptyList()
}

fun SharedPreferences.checkIpAndPortInCmd(): Pair<String?, String?> {
    val cmdEnable = getBoolean("byedpi_enable_cmd_settings", false)
    if (!cmdEnable) return Pair(null, null)

    val cmdArgs = getString("byedpi_cmd_args", "")?.let { shellSplit(it) } ?: emptyList()

    fun getArgValue(argsList: List<String>, keys: List<String>): String? {
        for (i in argsList.indices) {
            val arg = argsList[i]
            for (key in keys) {
                if (key.startsWith("--")) {
                    if (arg == key && i + 1 < argsList.size) {
                        return argsList[i + 1]
                    } else if (arg.startsWith("$key=")) {
                        return arg.substringAfter('=')
                    }
                } else if (key.startsWith("-")) {
                    if (arg.startsWith(key) && arg.length > key.length) {
                        return arg.substring(key.length)
                    } else if (arg == key && i + 1 < argsList.size) {
                        return argsList[i + 1]
                    }
                }
            }
        }
        return null
    }

    val cmdIp = getArgValue(cmdArgs, listOf("--ip", "-i"))
    val cmdPort = getArgValue(cmdArgs, listOf("--port", "-p"))

    return Pair(cmdIp, cmdPort)
}


fun SharedPreferences.getProxyIpAndPort(): Pair<String, String> {
    val (cmdIp, cmdPort) = checkIpAndPortInCmd()

    val ip = cmdIp ?: getStringNotNull("byedpi_proxy_ip", "127.0.0.1")
    val port = cmdPort ?: getStringNotNull("byedpi_proxy_port", "1080")

    return Pair(ip, port)
}

class AppPreferences(private val prefs: SharedPreferences) {
    var language: String
        get() = prefs.getString("language", "system") ?: "system"
        set(value) = prefs.edit().putString("language", value).apply()

    var theme: String
        get() = prefs.getString("app_theme", "system") ?: "system"
        set(value) = prefs.edit().putString("app_theme", value).apply()

    var colorScheme: String
        get() = prefs.getString("color_scheme", "Default") ?: "Default"
        set(value) = prefs.edit().putString("color_scheme", value).apply()

    var mode: Mode
        get() = prefs.mode()
        set(value) = prefs.edit().putString("byedpi_mode", value.toString().lowercase()).apply()

    var dnsIp: String
        get() = prefs.getString("dns_ip", "1.1.1.1") ?: "1.1.1.1"
        set(value) = prefs.edit().putString("dns_ip", value).apply()

    var dnsSolution: String
        get() = prefs.getString("dns_solution", "1.1.1.1") ?: "1.1.1.1"
        set(value) = prefs.edit().putString("dns_solution", value).apply()

    var ipv6Enable: Boolean
        get() = prefs.getBoolean("ipv6_enable", false)
        set(value) = prefs.edit().putBoolean("ipv6_enable", value).apply()

    var applistType: String
        get() = prefs.getString("applist_type", "disable") ?: "disable"
        set(value) = prefs.edit().putString("applist_type", value).apply()

    var autostart: Boolean
        get() = prefs.getBoolean("autostart", false)
        set(value) = prefs.edit().putBoolean("autostart", value).apply()

    var autoConnect: Boolean
        get() = prefs.getBoolean("auto_connect", false)
        set(value) = prefs.edit().putBoolean("auto_connect", value).apply()

    var cmdEnable: Boolean
        get() = prefs.getBoolean("byedpi_enable_cmd_settings", false)
        set(value) = prefs.edit().putBoolean("byedpi_enable_cmd_settings", value).apply()

    var proxyIp: String
        get() = prefs.getString("byedpi_proxy_ip", "127.0.0.1") ?: "127.0.0.1"
        set(value) = prefs.edit().putString("byedpi_proxy_ip", value).apply()

    var proxyPort: String
        get() = prefs.getString("byedpi_proxy_port", "1080") ?: "1080"
        set(value) = prefs.edit().putString("byedpi_proxy_port", value).apply()

    var httpConnect: Boolean
        get() = prefs.getBoolean("byedpi_http_connect", false)
        set(value) = prefs.edit().putBoolean("byedpi_http_connect", value).apply()

    var cmdArgs: String
        get() = prefs.getString("byedpi_cmd_args", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_cmd_args", value).apply()
        
    var wifiProfile: String
        get() = prefs.getString("byedpi_wifi_profile", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_wifi_profile", value).apply()
        
    var mobileProfile: String
        get() = prefs.getString("byedpi_mobile_profile", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_mobile_profile", value).apply()

    fun getProfileName(command: String): String? {
        try {
            val historyJson = prefs.getString("byedpi_command_history", null) ?: return null
            val history = Gson().fromJson(historyJson, Array<Command>::class.java)
            return history.find { it.text == command }?.name
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

class TestPreferences(private val prefs: SharedPreferences) {
    var delay: String
        get() = prefs.getString("byedpi_proxytest_delay", "6") ?: "6"
        set(value) = prefs.edit().putString("byedpi_proxytest_delay", value).apply()

    var requests: String
        get() = prefs.getString("byedpi_proxytest_requests", "1") ?: "1"
        set(value) = prefs.edit().putString("byedpi_proxytest_requests", value).apply()

    var timeout: String
        get() = prefs.getString("byedpi_proxytest_timeout", "5") ?: "5"
        set(value) = prefs.edit().putString("byedpi_proxytest_timeout", value).apply()

    var sni: String
        get() = prefs.getString("byedpi_proxytest_sni", "max.ru") ?: "max.ru"
        set(value) = prefs.edit().putString("byedpi_proxytest_sni", value).apply()

    var fullLog: Boolean
        get() = prefs.getBoolean("byedpi_proxytest_fulllog", false)
        set(value) = prefs.edit().putBoolean("byedpi_proxytest_fulllog", value).apply()

    var logClickable: Boolean
        get() = prefs.getBoolean("byedpi_proxytest_logclickable", false)
        set(value) = prefs.edit().putBoolean("byedpi_proxytest_logclickable", value).apply()

    var autoSort: Boolean
        get() = prefs.getBoolean("byedpi_proxytest_autosort", true)
        set(value) = prefs.edit().putBoolean("byedpi_proxytest_autosort", value).apply()

    var showAll: Boolean
        get() = prefs.getBoolean("byedpi_proxytest_showall", false)
        set(value) = prefs.edit().putBoolean("byedpi_proxytest_showall", value).apply()

    var domainLists: Set<String>
        get() = prefs.getStringSet("byedpi_proxytest_domain_lists", setOf("youtube", "googlevideo", "social")) ?: setOf("youtube", "googlevideo", "social")
        set(value) = prefs.edit().putStringSet("byedpi_proxytest_domain_lists", value).apply()

    var domains: String
        get() = prefs.getString("byedpi_proxytest_domains", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_proxytest_domains", value).apply()

    var strategyLists: Set<String>
        get() = prefs.getStringSet("byedpi_proxytest_strategy_lists", setOf("builtin")) ?: setOf("builtin")
        set(value) = prefs.edit().putStringSet("byedpi_proxytest_strategy_lists", value).apply()

    var commands: String
        get() = prefs.getString("byedpi_proxytest_commands", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_proxytest_commands", value).apply()

    var concurrentRequests: String
        get() = prefs.getString("byedpi_proxytest_concurrent_requests", "20") ?: "20"
        set(value) = prefs.edit().putString("byedpi_proxytest_concurrent_requests", value).apply()
}

class UIPreferences(private val prefs: SharedPreferences) {
    var maxConnections: String
        get() = prefs.getString("byedpi_max_connections", "512") ?: "512"
        set(value) = prefs.edit().putString("byedpi_max_connections", value).apply()

    var bufferSize: String
        get() = prefs.getString("byedpi_buffer_size", "16384") ?: "16384"
        set(value) = prefs.edit().putString("byedpi_buffer_size", value).apply()

    var noDomain: Boolean
        get() = prefs.getBoolean("byedpi_no_domain", false)
        set(value) = prefs.edit().putBoolean("byedpi_no_domain", value).apply()

    var tcpFastOpen: Boolean
        get() = prefs.getBoolean("byedpi_tcp_fast_open", false)
        set(value) = prefs.edit().putBoolean("byedpi_tcp_fast_open", value).apply()

    var desyncMethod: String
        get() = prefs.getString("byedpi_desync_method", "oob") ?: "oob"
        set(value) = prefs.edit().putString("byedpi_desync_method", value).apply()

    var hostsMode: String
        get() = prefs.getString("byedpi_hosts_mode", "disable") ?: "disable"
        set(value) = prefs.edit().putString("byedpi_hosts_mode", value).apply()

    var hostsBlacklist: String
        get() = prefs.getString("byedpi_hosts_blacklist", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_hosts_blacklist", value).apply()

    var hostsWhitelist: String
        get() = prefs.getString("byedpi_hosts_whitelist", "") ?: ""
        set(value) = prefs.edit().putString("byedpi_hosts_whitelist", value).apply()

    var defaultTtl: String
        get() = prefs.getString("byedpi_default_ttl", "0") ?: "0"
        set(value) = prefs.edit().putString("byedpi_default_ttl", value).apply()

    var splitPosition: String
        get() = prefs.getString("byedpi_split_position", "1") ?: "1"
        set(value) = prefs.edit().putString("byedpi_split_position", value).apply()

    var splitAtHost: Boolean
        get() = prefs.getBoolean("byedpi_split_at_host", false)
        set(value) = prefs.edit().putBoolean("byedpi_split_at_host", value).apply()

    var dropSack: Boolean
        get() = prefs.getBoolean("byedpi_drop_sack", false)
        set(value) = prefs.edit().putBoolean("byedpi_drop_sack", value).apply()

    var fakeTtl: String
        get() = prefs.getString("byedpi_fake_ttl", "8") ?: "8"
        set(value) = prefs.edit().putString("byedpi_fake_ttl", value).apply()

    var fakeOffset: String
        get() = prefs.getString("byedpi_fake_offset", "0") ?: "0"
        set(value) = prefs.edit().putString("byedpi_fake_offset", value).apply()

    var fakeSni: String
        get() = prefs.getString("byedpi_fake_sni", "www.iana.org") ?: "www.iana.org"
        set(value) = prefs.edit().putString("byedpi_fake_sni", value).apply()

    var oobData: String
        get() = prefs.getString("byedpi_oob_data", "a") ?: "a"
        set(value) = prefs.edit().putString("byedpi_oob_data", value).apply()

    var desyncHttp: Boolean
        get() = prefs.getBoolean("byedpi_desync_http", true)
        set(value) = prefs.edit().putBoolean("byedpi_desync_http", value).apply()

    var desyncHttps: Boolean
        get() = prefs.getBoolean("byedpi_desync_https", true)
        set(value) = prefs.edit().putBoolean("byedpi_desync_https", value).apply()

    var desyncUdp: Boolean
        get() = prefs.getBoolean("byedpi_desync_udp", true)
        set(value) = prefs.edit().putBoolean("byedpi_desync_udp", value).apply()

    var hostMixedCase: Boolean
        get() = prefs.getBoolean("byedpi_host_mixed_case", false)
        set(value) = prefs.edit().putBoolean("byedpi_host_mixed_case", value).apply()

    var domainMixedCase: Boolean
        get() = prefs.getBoolean("byedpi_domain_mixed_case", false)
        set(value) = prefs.edit().putBoolean("byedpi_domain_mixed_case", value).apply()

    var hostRemoveSpaces: Boolean
        get() = prefs.getBoolean("byedpi_host_remove_spaces", false)
        set(value) = prefs.edit().putBoolean("byedpi_host_remove_spaces", value).apply()

    var tlsRecEnabled: Boolean
        get() = prefs.getBoolean("byedpi_tlsrec_enabled", false)
        set(value) = prefs.edit().putBoolean("byedpi_tlsrec_enabled", value).apply()

    var tlsRecPosition: String
        get() = prefs.getString("byedpi_tlsrec_position", "0") ?: "0"
        set(value) = prefs.edit().putString("byedpi_tlsrec_position", value).apply()

    var tlsRecAtSni: Boolean
        get() = prefs.getBoolean("byedpi_tlsrec_at_sni", false)
        set(value) = prefs.edit().putBoolean("byedpi_tlsrec_at_sni", value).apply()

    var udpFakeCount: String
        get() = prefs.getString("byedpi_udp_fake_count", "1") ?: "1"
        set(value) = prefs.edit().putString("byedpi_udp_fake_count", value).apply()
}
