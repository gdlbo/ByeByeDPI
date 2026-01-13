package io.github.dovecoteescapee.byedpi.utility

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

fun Long.toReadableDateTime(): String {
    val format = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
    return format.format(this)
}

fun Context.isBatteryOptimizationEnabled(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(packageName)
}

fun Context.isTv(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

fun Context.hasStorageAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.getPrivateDnsMode(): String? {
    return try {
        Settings.Global.getString(contentResolver, "private_dns_mode")
    } catch (e: Exception) {
        null
    }
}

fun Context.getPrivateDnsSpecifier(): String? {
    return try {
        Settings.Global.getString(contentResolver, "private_dns_specifier")
    } catch (e: Exception) {
        null
    }
}
