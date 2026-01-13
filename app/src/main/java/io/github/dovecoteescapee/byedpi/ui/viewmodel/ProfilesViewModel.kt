package io.github.dovecoteescapee.byedpi.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.AppPreferences
import io.github.dovecoteescapee.byedpi.utility.HistoryUtils
import io.github.dovecoteescapee.byedpi.utility.getPreferences

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {
    private val historyUtils = HistoryUtils(application)
    private val appPrefs = AppPreferences(application.getPreferences())

    var profiles = mutableStateListOf<Command>()
        private set
        
    var wifiProfile by mutableStateOf(appPrefs.wifiProfile)
        private set
        
    var mobileProfile by mutableStateOf(appPrefs.mobileProfile)
        private set

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        profiles.clear()
        profiles.addAll(historyUtils.getHistory().filter { it.pinned })
    }

    fun addProfile(name: String, command: String) {
        historyUtils.addCommand(command)
        historyUtils.pinCommand(command)
        if (name.isNotBlank()) {
            historyUtils.renameCommand(command, name)
        }
        loadProfiles()
    }

    fun updateProfile(profile: Command, name: String, command: String) {
        if (profile.text != command) {
            historyUtils.editCommand(profile.text, command)
        }
        historyUtils.renameCommand(command, name)
        loadProfiles()
    }

    fun deleteProfile(profile: Command) {
        historyUtils.unpinCommand(profile.text)
        if (wifiProfile == profile.text) updateWifiProfile("")
        if (mobileProfile == profile.text) updateMobileProfile("")
        loadProfiles()
    }

    fun applyProfile(profile: Command) {
        appPrefs.cmdArgs = profile.text
        appPrefs.cmdEnable = true
        
        if (appStatus.first == AppStatus.Running) {
            ServiceManager.restart(getApplication(), appPrefs.mode)
        }
    }
    
    fun updateWifiProfile(command: String) {
        appPrefs.wifiProfile = command
        wifiProfile = command
    }
    
    fun updateMobileProfile(command: String) {
        appPrefs.mobileProfile = command
        mobileProfile = command
    }
}
