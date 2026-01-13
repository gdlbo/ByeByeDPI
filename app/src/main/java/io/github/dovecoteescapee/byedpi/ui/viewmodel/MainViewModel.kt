package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.net.VpnService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.AppPreferences
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.getProxyIpAndPort
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getPreferences()
    private val appPrefs = AppPreferences(prefs)

    var isClickable by mutableStateOf(true)
        private set

    val currentStatus get() = appStatus.first
    val currentMode get() = appStatus.second
    
    var preferredMode by mutableStateOf(appPrefs.mode)
        private set
        
    var isCmdEnabled by mutableStateOf(appPrefs.cmdEnable)
        private set

    var currentProfileName by mutableStateOf(appPrefs.getProfileName(appPrefs.cmdArgs))
        private set

    val proxyAddress: Pair<String, String>
        get() = prefs.getProxyIpAndPort()

    private val _toastEvent = MutableSharedFlow<Int>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "byedpi_mode" -> preferredMode = appPrefs.mode
            "byedpi_enable_cmd_settings" -> isCmdEnabled = appPrefs.cmdEnable
            "byedpi_cmd_args" -> currentProfileName = appPrefs.getProfileName(appPrefs.cmdArgs)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onCleared()
    }

    private fun showToast(messageResId: Int) {
        viewModelScope.launch {
            _toastEvent.emit(messageResId)
        }
    }

    fun setMode(mode: Mode) {
        if (currentStatus == AppStatus.Running) {
            showToast(R.string.settings_unavailable)
            return
        }
        appPrefs.mode = mode
    }

    fun performActionIfStopped(action: () -> Unit) {
        if (currentStatus == AppStatus.Running) {
            showToast(R.string.settings_unavailable)
        } else {
            action()
        }
    }

    fun toggleService(onPrepareVpn: (Intent) -> Unit) {
        if (!isClickable) return
        
        isClickable = false
        viewModelScope.launch {
            if (currentStatus == AppStatus.Halted) {
                startService(onPrepareVpn)
            } else {
                stopService()
            }
            delay(1000)
            isClickable = true
        }
    }

    private fun startService(onPrepareVpn: (Intent) -> Unit) {
        val context = getApplication<Application>()
        when (preferredMode) {
            Mode.VPN -> {
                val intentPrepare = VpnService.prepare(context)
                if (intentPrepare != null) {
                    onPrepareVpn(intentPrepare)
                } else {
                    ServiceManager.start(context, Mode.VPN)
                }
            }
            Mode.Proxy -> ServiceManager.start(context, Mode.Proxy)
        }
    }

    private fun stopService() {
        ServiceManager.stop(getApplication())
    }
}

private typealias Intent = android.content.Intent
