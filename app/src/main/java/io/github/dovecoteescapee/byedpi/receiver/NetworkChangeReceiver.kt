package io.github.dovecoteescapee.byedpi.receiver

import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.services.ByeDpiProxyService
import io.github.dovecoteescapee.byedpi.services.ByeDpiVpnService
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.utility.AppPreferences
import io.github.dovecoteescapee.byedpi.utility.createConnectionNotification
import io.github.dovecoteescapee.byedpi.utility.getPreferences

object NetworkChangeReceiver {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun register(context: Context) {
        if (networkCallback != null) return

        val appContext = context.applicationContext
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handleNetworkChange(appContext)
            }

            override fun onLost(network: Network) {
                handleNetworkChange(appContext)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                handleNetworkChange(appContext)
            }
        }

        networkCallback = callback

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun unregister(context: Context) {
        networkCallback?.let {
            val appContext = context.applicationContext
            val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: Exception) {
                // Ignore
            }
            networkCallback = null
        }
    }

    private fun handleNetworkChange(context: Context) {
        val prefs = AppPreferences(context.getPreferences())
        val wifiProfile = prefs.wifiProfile
        val mobileProfile = prefs.mobileProfile

        if (wifiProfile.isEmpty() && mobileProfile.isEmpty()) return

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        var isWifi = false
        var isCellular = false

        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val caps = connectivityManager.getNetworkCapabilities(network) ?: continue
            
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) continue
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) continue

            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isWifi = true
                break 
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isCellular = true
            }
        }

        val targetProfile = when {
            isWifi -> wifiProfile
            isCellular -> mobileProfile
            else -> ""
        }

        if (targetProfile.isNotEmpty() && targetProfile != prefs.cmdArgs) {
            prefs.cmdArgs = targetProfile
            prefs.cmdEnable = true

            if (appStatus.first == AppStatus.Running) {
                ServiceManager.restart(context, prefs.mode)
                
                val profileName = prefs.getProfileName(targetProfile)
                showRestartNotification(context, profileName)
            }
        }
    }

    private fun showRestartNotification(context: Context, profileName: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createConnectionNotification(
            context,
            if (ServiceManager.isVpnMode()) "ByeDPIVpn" else "ByeDPI Proxy",
            R.string.notification_title,
            R.string.restarting_service,
            if (ServiceManager.isVpnMode()) ByeDpiVpnService::class.java else ByeDpiProxyService::class.java,
            profileName
        )
        notificationManager.notify(if (ServiceManager.isVpnMode()) 1 else 2, notification)
    }
}
