package io.github.dovecoteescapee.byedpi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.BuildConfig
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.ui.components.*
import io.github.dovecoteescapee.byedpi.ui.viewmodel.SettingsViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit,
    onReset: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onNavigateToTest: () -> Unit = {},
    onNavigateToAppSelection: () -> Unit = {},
    onNavigateToCmdSettings: () -> Unit = {},
    onNavigateToUISettings: () -> Unit = {},
    onOpenTelegram: () -> Unit = {},
    onOpenSourceCode: () -> Unit = {},
    onRequestStorageAccess: () -> Unit = {},
    onRequestDisableBatteryOptimization: () -> Unit = {}
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    var showMenu by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshBatteryOptimizationStatus()
        viewModel.refreshStorageAccessStatus()
        viewModel.refreshPrivateDnsStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.reset_settings)) },
                            leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onReset()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_settings)) },
                            leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onExport()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_settings)) },
                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onImport()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(if (isTv) 48.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(title = stringResource(R.string.private_dns)) {
                    val privateDnsMode = viewModel.privateDnsMode ?: "off"
                    val privateDnsSummary = when (privateDnsMode) {
                        "off" -> stringResource(R.string.private_dns_off)
                        "opportunistic" -> stringResource(R.string.private_dns_auto)
                        "hostname" -> viewModel.privateDnsSpecifier ?: stringResource(R.string.private_dns_provider)
                        else -> privateDnsMode
                    }

                    PreferenceItem(
                        title = stringResource(R.string.private_dns),
                        summary = privateDnsSummary,
                        icon = Icons.Default.VpnLock
                    )

                    if (privateDnsMode == "off" || privateDnsMode == "opportunistic") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.private_dns_suggest_cloudflare),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.private_dns_how_to_use),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Cloudflare DNS", "1dot1dot1dot1.cloudflare-dns.com")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.copy_cloudflare_dns))
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.general_category)) {
                    val modes = stringArrayResource(R.array.byedpi_modes)
                    val modeValues = stringArrayResource(R.array.byedpi_modes_entries)
                    val modeMap = modeValues.zip(modes).toMap()

                    ListPreference(
                        title = stringResource(R.string.mode_setting),
                        value = viewModel.mode.toString().lowercase(),
                        entries = modeMap,
                        onValueChange = { viewModel.updateMode(it) },
                        icon = Icons.Default.SettingsInputComponent
                    )

                    AnimatedVisibility(
                        visible = viewModel.mode == Mode.VPN,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            val dnsSolutions = stringArrayResource(R.array.dns_solutions)
                            val dnsSolutionValues = stringArrayResource(R.array.dns_solutions_entries)
                            val dnsSolutionMap = dnsSolutionValues.zip(dnsSolutions).toMap()

                            ListPreference(
                                title = stringResource(R.string.dns_solution_setting),
                                value = viewModel.dnsSolution,
                                entries = dnsSolutionMap,
                                onValueChange = { viewModel.updateDnsSolution(it) },
                                icon = Icons.Default.Dns
                            )

                            AnimatedVisibility(
                                visible = viewModel.dnsSolution == "custom",
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                EditTextPreference(
                                    title = stringResource(R.string.dbs_ip_setting),
                                    value = viewModel.dnsIp,
                                    onValueChange = { viewModel.updateDns(it) },
                                    icon = Icons.Default.Dns
                                )
                            }

                            SwitchPreference(
                                title = stringResource(R.string.ipv6_setting),
                                checked = viewModel.ipv6Enable,
                                onCheckedChange = { viewModel.updateIpv6(it) },
                                icon = Icons.Default.NetworkCheck
                            )

                            val applistTypes = stringArrayResource(R.array.applist_types)
                            val applistValues = stringArrayResource(R.array.applist_types_entries)
                            val applistMap = applistValues.zip(applistTypes).toMap()

                            ListPreference(
                                title = stringResource(R.string.applist_setting),
                                value = viewModel.applistType,
                                entries = applistMap,
                                onValueChange = { viewModel.updateApplistType(it) },
                                icon = Icons.Default.FilterList
                            )

                            AnimatedVisibility(
                                visible = viewModel.applistType != "disable",
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                PreferenceItem(
                                    title = stringResource(R.string.apps_select),
                                    onClick = onNavigateToAppSelection,
                                    icon = Icons.Default.AppRegistration
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.appearance_category)) {
                    val languages = stringArrayResource(R.array.languages)
                    val languageValues = stringArrayResource(R.array.languages_entries)
                    val languageMap = languageValues.zip(languages).toMap()

                    ListPreference(
                        title = stringResource(R.string.lang_settings),
                        value = viewModel.language,
                        entries = languageMap,
                        onValueChange = { viewModel.updateLanguage(it) },
                        icon = Icons.Default.Language
                    )

                    val themes = stringArrayResource(R.array.themes)
                    val themeValues = stringArrayResource(R.array.themes_entries)
                    val themeMap = themeValues.zip(themes).toMap()

                    ListPreference(
                        title = stringResource(R.string.theme_settings),
                        value = viewModel.theme,
                        entries = themeMap,
                        onValueChange = { viewModel.updateTheme(it) },
                        icon = Icons.Default.Palette
                    )

                    val colorSchemes = stringArrayResource(R.array.color_schemes)
                    val colorSchemeValues = stringArrayResource(R.array.color_schemes_entries)
                    val colorSchemeMap = colorSchemeValues.zip(colorSchemes).toMap()

                    ListPreference(
                        title = stringResource(R.string.color_scheme_settings),
                        value = viewModel.colorScheme,
                        entries = colorSchemeMap,
                        onValueChange = { viewModel.updateColorScheme(it) },
                        icon = Icons.Default.ColorLens,
                        enabled = !viewModel.dynamicColors
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SwitchPreference(
                            title = stringResource(R.string.dynamic_colors_settings),
                            checked = viewModel.dynamicColors,
                            onCheckedChange = { viewModel.updateDynamicColors(it) },
                            icon = Icons.Default.AutoFixHigh
                        )
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.byedpi_category)) {
                    SwitchPreference(
                        title = stringResource(R.string.use_command_line_settings),
                        checked = viewModel.cmdEnable,
                        onCheckedChange = { viewModel.updateCmdEnable(it) },
                        icon = Icons.Default.Code
                    )

                    PreferenceItem(
                        title = stringResource(R.string.ui_editor),
                        enabled = !viewModel.cmdEnable,
                        onClick = onNavigateToUISettings,
                        icon = Icons.Default.EditNote
                    )

                    PreferenceItem(
                        title = stringResource(R.string.command_line_editor),
                        enabled = viewModel.cmdEnable,
                        onClick = onNavigateToCmdSettings,
                        icon = Icons.Default.Terminal
                    )

                    PreferenceItem(
                        title = stringResource(R.string.title_test),
                        summary = stringResource(R.string.summary_test),
                        enabled = viewModel.cmdEnable,
                        onClick = onNavigateToTest,
                        icon = Icons.Default.BugReport
                    )

                    AnimatedVisibility(
                        visible = !viewModel.hasStorageAccess,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        PreferenceItem(
                            title = stringResource(R.string.storage_access),
                            summary = stringResource(R.string.storage_access_summary),
                            onClick = onRequestStorageAccess,
                            icon = Icons.Default.Storage
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = viewModel.isProxyVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SettingsCard(title = stringResource(R.string.byedpi_proxy)) {
                        EditTextPreference(
                            title = stringResource(R.string.bye_dpi_proxy_ip_setting),
                            value = viewModel.proxyIp,
                            onValueChange = { viewModel.updateProxyIp(it) },
                            icon = Icons.Default.Router
                        )

                        EditTextPreference(
                            title = stringResource(R.string.byedpi_proxy_port_setting),
                            placeholder = stringResource(R.string.byedpi_proxy_port_setting_hint),
                            value = viewModel.proxyPort,
                            onValueChange = { viewModel.updateProxyPort(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            icon = Icons.Default.Numbers
                        )

                        SwitchPreference(
                            title = stringResource(R.string.byedpi_http_connect_setting),
                            summary = stringResource(R.string.byedpi_http_connect_summary),
                            checked = viewModel.httpConnect,
                            onCheckedChange = { viewModel.updateHttpConnect(it) },
                            icon = Icons.Default.Http
                        )
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.automation)) {
                    SwitchPreference(
                        title = stringResource(R.string.autostart_settings),
                        checked = viewModel.autostart,
                        onCheckedChange = { viewModel.updateAutostart(it) },
                        icon = Icons.Default.PowerSettingsNew
                    )

                    SwitchPreference(
                        title = stringResource(R.string.autoconnect_settings),
                        checked = viewModel.autoConnect,
                        onCheckedChange = { viewModel.updateAutoConnect(it) },
                        icon = Icons.Default.AutoMode
                    )

                    AnimatedVisibility(
                        visible = viewModel.isBatteryOptimizationEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        PreferenceItem(
                            title = stringResource(R.string.battery_optimization),
                            summary = stringResource(R.string.battery_optimization_summary),
                            onClick = onRequestDisableBatteryOptimization,
                            icon = Icons.Default.BatteryAlert
                        )
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.about_category)) {
                    PreferenceItem(
                        title = stringResource(R.string.telegram_link),
                        onClick = onOpenTelegram,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    PreferenceItem(
                        title = stringResource(R.string.source_code_link),
                        onClick = onOpenSourceCode,
                        icon = Icons.Default.Source
                    )

                    PreferenceItem(
                        title = stringResource(R.string.version),
                        summary = BuildConfig.VERSION_NAME,
                        icon = Icons.Default.Info
                    )

                    PreferenceItem(
                        title = stringResource(R.string.byedpi_version),
                        summary = "0.17.3",
                        icon = Icons.Default.HistoryEdu
                    )
                }
            }
        }
    }
}
