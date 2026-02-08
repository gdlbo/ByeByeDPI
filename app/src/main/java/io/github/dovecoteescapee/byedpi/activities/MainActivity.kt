package io.github.dovecoteescapee.byedpi.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.*
import io.github.dovecoteescapee.byedpi.services.ServiceManager
import io.github.dovecoteescapee.byedpi.services.appStatus
import io.github.dovecoteescapee.byedpi.ui.*
import io.github.dovecoteescapee.byedpi.ui.theme.TrackerTheme
import io.github.dovecoteescapee.byedpi.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private val logsRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { log ->
            lifecycleScope.launch(Dispatchers.IO) {
                val uri = log.data?.data ?: run {
                    Log.e(TAG, "No data in result")
                    return@launch
                }
                LogUtils.saveLogs(this@MainActivity, uri)
            }
        }

    private val exportSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                SettingsUtils.exportSettings(this@MainActivity, it)
            }
        }
    }

    private val importSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                SettingsUtils.importSettings(this@MainActivity, it) {
                    runOnUiThread { recreate() }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, R.string.storage_access_allowed_summary, Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received intent: ${intent?.action}")

            if (intent == null) {
                Log.w(TAG, "Received null intent")
                return
            }

            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd)
            if (sender == null) {
                Log.w(TAG, "Received intent with unknown sender: $senderOrd")
                return
            }

            when (val action = intent.action) {
                STARTED_BROADCAST,
                STOPPED_BROADCAST -> { /* appStatus is updated via mutableStateOf in ByeDpiStatus */ }

                FAILED_BROADCAST -> {
                    Toast.makeText(
                        context,
                        getString(R.string.failed_to_start, sender.name),
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                else -> Log.w(TAG, "Unknown action: $action")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getPreferences()
        val lang = prefs.getStringNotNull("language", "system")
        SettingsUtils.setLang(lang)
        val theme = prefs.getStringNotNull("app_theme", "system")
        SettingsUtils.setTheme(theme)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        if (getPreferences().getBoolean("auto_connect", false) && appStatus.first != AppStatus.Running) {
            start()
        }

        ShortcutUtils.update(this)

        handleIntent(intent)

        setContent {
            val themeManager = remember { ThemeManager(this) }
            TrackerTheme(themeManager = themeManager) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val isTv = remember { context.isTv() }

                var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                LaunchedEffect(Unit) {
                    if (!isTv && isBatteryOptimizationEnabled()) {
                        showBatteryOptimizationDialog = true
                    }
                }

                if (showBatteryOptimizationDialog) {
                    if (isTv) {
                        AlertDialog(
                            onDismissRequest = { showBatteryOptimizationDialog = false },
                            title = { Text(stringResource(R.string.battery_optimization_dialog_title)) },
                            text = { Text(stringResource(R.string.battery_optimization_dialog_message)) },
                            confirmButton = {
                                Button(onClick = {
                                    showBatteryOptimizationDialog = false
                                    requestDisableBatteryOptimization()
                                }) {
                                    Text(stringResource(R.string.battery_optimization_disable))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBatteryOptimizationDialog = false }) {
                                    Text(stringResource(R.string.action_cancel))
                                }
                            }
                        )
                    } else {
                        ModalBottomSheet(
                            onDismissRequest = { showBatteryOptimizationDialog = false },
                            sheetState = sheetState,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 0.dp,
                            dragHandle = { BottomSheetDefaults.DragHandle() }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 24.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.battery_optimization_dialog_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.battery_optimization_dialog_message),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { showBatteryOptimizationDialog = false },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    ) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                    Button(
                                        onClick = {
                                            showBatteryOptimizationDialog = false
                                            requestDisableBatteryOptimization()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    ) {
                                        Text(stringResource(R.string.battery_optimization_disable))
                                    }
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(intent?.getStringExtra("navigate_to")) {
                    intent?.getStringExtra("navigate_to")?.let {
                        navController.navigate(it)
                        intent?.removeExtra("navigate_to")
                    }
                }

                val offsetSpec = tween<IntOffset>(
                    durationMillis = 400,
                    easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                )
                val springSpec = remember { spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 0.1f) }

                NavHost(
                    navController = navController, startDestination = "home",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = offsetSpec
                        ) + fadeIn(animationSpec = springSpec)
                    },
                    exitTransition = {
                        slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = offsetSpec
                    ) + fadeOut(animationSpec = springSpec)
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = offsetSpec
                    ) + fadeIn(animationSpec = springSpec)  },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = offsetSpec
                        ) + fadeOut(animationSpec = springSpec)
                    }
                ) {
                    composable("home",
                        exitTransition = { fadeOut(animationSpec = springSpec) },
                        popEnterTransition = { fadeIn(animationSpec = springSpec) }
                    ) {
                        MainScreen(
                            onPrepareVpn = { vpnRegister.launch(it) },
                            onOpenSettings = {
                                navController.navigate("settings")
                            },
                            onSaveLogs = { saveLogs() },
                            onCloseApp = { closeApp() },
                            onOpenEditor = {
                                if (getPreferences().getBoolean("byedpi_enable_cmd_settings", false)) {
                                    navController.navigate("settings/cmd")
                                } else {
                                    navController.navigate("settings/ui")
                                }
                            },
                            onOpenProfiles = {
                                navController.navigate("profiles")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onReset = {
                                getPreferences().edit { clear() }
                                SettingsUtils.setTheme("system")
                                recreate()
                                navController.navigate("home")
                            },
                            onExport = {
                                val fileName = "bbd_${System.currentTimeMillis().toReadableDateTime()}.json"
                                exportSettingsLauncher.launch(fileName)
                            },
                            onImport = {
                                importSettingsLauncher.launch(arrayOf("application/json"))
                                navController.popBackStack()
                            },
                            onNavigateToTest = {
                                navController.navigate("test")
                            },
                            onNavigateToAppSelection = {
                                navController.navigate("settings/apps")
                            },
                            onNavigateToCmdSettings = {
                                navController.navigate("settings/cmd")
                            },
                            onNavigateToUISettings = {
                                navController.navigate("settings/ui")
                            },
                            onOpenTelegram = {
                                openUrl("https://t.me/gdlbo")
                            },
                            onOpenSourceCode = {
                                openUrl("https://github.com/gdlbo/ByeByeDPI")
                            },
                            onRequestStorageAccess = {
                                requestStoragePermission()
                            },
                            onRequestDisableBatteryOptimization = {
                                requestDisableBatteryOptimization()
                            },
                            onThemeChange = {
                                if (isTv) recreate()
                            }
                        )
                    }
                    composable("settings/cmd") {
                        CmdSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/ui") {
                        UISettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/apps") {
                        AppSelectionScreen(onBack = { navController.popBackStack() })
                    }
                    composable("test") {
                        TestScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onOpenSettings = {
                                navController.navigate("settings/test")
                            }
                        )
                    }
                    composable("settings/test") {
                        TestSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("profiles") {
                        ProfilesScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToTest = { navController.navigate("test") }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_TOGGLE) {
            val strategy = intent.getStringExtra("strategy")
            val prefs = getPreferences()
            var updated = false
            if (strategy != null && strategy != prefs.getString("byedpi_cmd_args", null)) {
                prefs.edit(commit = true) { putString("byedpi_cmd_args", strategy) }
                updated = true
            }

            val onlyUpdate = intent.getBooleanExtra("only_update", false)
            val onlyStart = intent.getBooleanExtra("only_start", false)
            val onlyStop = intent.getBooleanExtra("only_stop", false)

            when {
                onlyUpdate -> Log.i(TAG, "Only update strategy")
                onlyStart -> if (appStatus.first == AppStatus.Halted) start()
                onlyStop -> if (appStatus.first == AppStatus.Running) stop()
                else -> {
                    if (appStatus.first == AppStatus.Halted) {
                        start()
                    } else {
                        if (updated) {
                            ServiceManager.restart(this, prefs.mode())
                        } else {
                            stop()
                        }
                    }
                }
            }
            
            if (intent.getBooleanExtra("finish_after", true)) {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    private fun start() {
        when (getPreferences().mode()) {
            Mode.VPN -> {
                val intentPrepare = android.net.VpnService.prepare(this)
                if (intentPrepare != null) {
                    vpnRegister.launch(intentPrepare)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                }
            }

            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop(this)
    }

    private fun saveLogs() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "byedpi.log")
        }
        logsRegister.launch(intent)
    }

    private fun closeApp() {
        val (status, _) = appStatus
        if (status == AppStatus.Running) stop()
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: $url", e)
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = "package:${packageName}".toUri()
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun requestDisableBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request ignore battery optimizations", e)
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings", e2)
            }
        }
    }
}
