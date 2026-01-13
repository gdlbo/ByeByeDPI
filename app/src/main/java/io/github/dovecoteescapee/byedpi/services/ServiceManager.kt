package io.github.dovecoteescapee.byedpi.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.data.START_ACTION
import io.github.dovecoteescapee.byedpi.data.STOP_ACTION
import kotlinx.coroutines.*

object ServiceManager {
    private val TAG: String = ServiceManager::class.java.simpleName
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun start(context: Context, mode: Mode) {
        when (mode) {
            Mode.VPN -> {
                Log.i(TAG, "Starting VPN")
                val intent = Intent(context, ByeDpiVpnService::class.java)
                intent.action = START_ACTION
                ContextCompat.startForegroundService(context, intent)
            }

            Mode.Proxy -> {
                Log.i(TAG, "Starting proxy")
                val intent = Intent(context, ByeDpiProxyService::class.java)
                intent.action = START_ACTION
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    fun stop(context: Context) {
        val (_, mode) = appStatus
        try {
            when (mode) {
                Mode.VPN -> {
                    Log.i(TAG, "Stopping VPN")
                    val intent = Intent(context, ByeDpiVpnService::class.java)
                    intent.action = STOP_ACTION
                    context.startService(intent)
                }

                Mode.Proxy -> {
                    Log.i(TAG, "Stopping proxy")
                    val intent = Intent(context, ByeDpiProxyService::class.java)
                    intent.action = STOP_ACTION
                    context.startService(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
        }
    }

    fun restart(context: Context, mode: Mode) {
        stop(context)
        scope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3000L) {
                if (appStatus.first == AppStatus.Halted) break
                delay(100)
            }
            start(context, mode)
        }
    }

    fun isVpnMode(): Boolean {
        return appStatus.second == Mode.VPN
    }
}
