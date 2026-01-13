package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.data.ThemeManager
import io.github.dovecoteescapee.byedpi.utility.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getPreferences()
    private val appPrefs = AppPreferences(prefs)
    private val themeManager = ThemeManager(application)

    var language by mutableStateOf(appPrefs.language)
        private set
    var theme by mutableStateOf(appPrefs.theme)
        private set
    var colorScheme by mutableStateOf(appPrefs.colorScheme)
        private set
    var mode by mutableStateOf(appPrefs.mode)
        private set
    var dnsIp by mutableStateOf(appPrefs.dnsIp)
        private set
    var dnsSolution by mutableStateOf(appPrefs.dnsSolution)
        private set
    var ipv6Enable by mutableStateOf(appPrefs.ipv6Enable)
        private set
    var applistType by mutableStateOf(appPrefs.applistType)
        private set
    var autostart by mutableStateOf(appPrefs.autostart)
        private set
    var autoConnect by mutableStateOf(appPrefs.autoConnect)
        private set
    var cmdEnable by mutableStateOf(appPrefs.cmdEnable)
        private set
    var proxyIp by mutableStateOf(appPrefs.proxyIp)
        private set
    var proxyPort by mutableStateOf(appPrefs.proxyPort)
        private set
    var httpConnect by mutableStateOf(appPrefs.httpConnect)
        private set
    var dynamicColors by mutableStateOf(themeManager.getDynamicColor())
        private set
    var isBatteryOptimizationEnabled by mutableStateOf(application.isBatteryOptimizationEnabled())
        private set
    var hasStorageAccess by mutableStateOf(application.hasStorageAccess())
        private set
    var privateDnsMode by mutableStateOf(application.getPrivateDnsMode())
        private set
    var privateDnsSpecifier by mutableStateOf(application.getPrivateDnsSpecifier())
        private set

    val isProxyVisible: Boolean
        get() {
            val (cmdIp, cmdPort) = prefs.checkIpAndPortInCmd()
            return !cmdEnable || (cmdIp == null && cmdPort == null)
        }

    fun updateLanguage(newValue: String) {
        appPrefs.language = newValue
        language = newValue
        SettingsUtils.setLang(newValue)
    }

    fun updateTheme(newValue: String) {
        appPrefs.theme = newValue
        theme = newValue
        themeManager.setDarkTheme(newValue)
    }

    fun updateColorScheme(newValue: String) {
        appPrefs.colorScheme = newValue
        colorScheme = newValue
        themeManager.setColorScheme(newValue)
    }

    fun updateDynamicColors(newValue: Boolean) {
        dynamicColors = newValue
        themeManager.setDynamicColor(newValue)
    }

    fun updateMode(newValue: String) {
        val newMode = Mode.fromString(newValue)
        appPrefs.mode = newMode
        mode = newMode
    }

    fun updateDns(newValue: String) {
        appPrefs.dnsIp = newValue
        dnsIp = newValue
    }

    fun updateDnsSolution(newValue: String) {
        appPrefs.dnsSolution = newValue
        dnsSolution = newValue
        if (newValue != "custom") {
            updateDns(newValue)
        }
    }

    fun updateIpv6(newValue: Boolean) {
        appPrefs.ipv6Enable = newValue
        ipv6Enable = newValue
    }

    fun updateApplistType(newValue: String) {
        appPrefs.applistType = newValue
        applistType = newValue
    }

    fun updateAutostart(newValue: Boolean) {
        appPrefs.autostart = newValue
        autostart = newValue
    }

    fun updateAutoConnect(newValue: Boolean) {
        appPrefs.autoConnect = newValue
        autoConnect = newValue
    }

    fun updateCmdEnable(newValue: Boolean) {
        appPrefs.cmdEnable = newValue
        cmdEnable = newValue
    }

    fun updateProxyIp(newValue: String) {
        appPrefs.proxyIp = newValue
        proxyIp = newValue
    }

    fun updateProxyPort(newValue: String) {
        appPrefs.proxyPort = newValue
        proxyPort = newValue
    }

    fun updateHttpConnect(newValue: Boolean) {
        appPrefs.httpConnect = newValue
        httpConnect = newValue
    }

    fun refreshBatteryOptimizationStatus() {
        isBatteryOptimizationEnabled = getApplication<Application>().isBatteryOptimizationEnabled()
    }

    fun refreshStorageAccessStatus() {
        hasStorageAccess = getApplication<Application>().hasStorageAccess()
    }

    fun refreshPrivateDnsStatus() {
        privateDnsMode = getApplication<Application>().getPrivateDnsMode()
        privateDnsSpecifier = getApplication<Application>().getPrivateDnsSpecifier()
    }
}
