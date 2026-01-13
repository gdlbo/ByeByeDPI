package io.github.dovecoteescapee.byedpi

import android.app.Application
import io.github.dovecoteescapee.byedpi.receiver.NetworkChangeReceiver

class ByeDpiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkChangeReceiver.register(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        NetworkChangeReceiver.unregister(this)
    }
}
