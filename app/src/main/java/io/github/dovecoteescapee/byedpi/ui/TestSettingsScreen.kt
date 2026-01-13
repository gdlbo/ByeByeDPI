package io.github.dovecoteescapee.byedpi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.ui.components.*
import io.github.dovecoteescapee.byedpi.ui.viewmodel.TestSettingsViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSettingsScreen(
    viewModel: TestSettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_test)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                SettingsCard(title = stringResource(R.string.byedpi_proxy)) {
                    EditTextPreference(
                        title = stringResource(R.string.test_delay),
                        value = viewModel.delay,
                        onValueChange = { viewModel.updateDelay(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Timer
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_requests),
                        value = viewModel.requests,
                        onValueChange = { viewModel.updateRequests(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Repeat
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_timeout),
                        value = viewModel.timeout,
                        onValueChange = { viewModel.updateTimeout(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.HourglassEmpty
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_concurrent_requests),
                        value = viewModel.concurrentRequests,
                        onValueChange = { viewModel.updateConcurrentRequests(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Speed
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_settings_sni),
                        value = viewModel.sni,
                        onValueChange = { viewModel.updateSni(it) },
                        icon = Icons.Default.Dns
                    )
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.test_settings_domain_lists)) {
                    val entries = stringArrayResource(R.array.domain_lists_entries)
                    val values = stringArrayResource(R.array.domain_lists_values)
                    val entryMap = values.zip(entries).toMap()

                    MultiSelectListPreference(
                        title = stringResource(R.string.test_settings_domain_lists),
                        values = viewModel.domainLists,
                        entries = entryMap,
                        onValuesChange = { viewModel.updateDomainLists(it) },
                        icon = Icons.AutoMirrored.Filled.List
                    )

                    AnimatedVisibility(
                        visible = viewModel.domainLists.contains("custom"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            ListEditPreference(
                                title = stringResource(R.string.test_settings_domains),
                                placeholder = stringResource(R.string.some_domain),
                                values = viewModel.domainsList,
                                onValuesChange = { viewModel.updateDomainsList(it) },
                                icon = Icons.Default.Add
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.test_settings_commands)) {
                    val entries = stringArrayResource(R.array.strategy_lists_entries)
                    val values = stringArrayResource(R.array.strategy_lists_values)
                    val entryMap = values.zip(entries).toMap()

                    MultiSelectListPreference(
                        title = stringResource(R.string.test_settings_usercommands),
                        values = viewModel.strategyLists,
                        entries = entryMap,
                        onValuesChange = { viewModel.updateStrategyLists(it) },
                        icon = Icons.Default.Terminal
                    )

                    AnimatedVisibility(
                        visible = viewModel.strategyLists.contains("custom"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            ListEditPreference(
                                title = stringResource(R.string.test_settings_commands),
                                placeholder = stringResource(R.string.some_strategy),
                                values = viewModel.commandsList,
                                onValuesChange = { viewModel.updateCommandsList(it) },
                                icon = Icons.Default.Code,
                                splitByLinesOnly = true
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.appearance_category)) {
                    SwitchPreference(
                        title = stringResource(R.string.test_settings_fulllog),
                        checked = viewModel.fullLog,
                        onCheckedChange = { viewModel.updateFullLog(it) },
                        icon = Icons.AutoMirrored.Filled.Notes
                    )

                    SwitchPreference(
                        title = stringResource(R.string.test_settings_logclickable),
                        checked = viewModel.logClickable,
                        onCheckedChange = { viewModel.updateLogClickable(it) },
                        icon = Icons.Default.TouchApp
                    )

                    SwitchPreference(
                        title = stringResource(R.string.test_settings_autosort),
                        checked = viewModel.autoSort,
                        onCheckedChange = { viewModel.updateAutoSort(it) },
                        icon = Icons.AutoMirrored.Filled.Sort
                    )

                    SwitchPreference(
                        title = stringResource(R.string.test_settings_showall),
                        checked = viewModel.showAll,
                        onCheckedChange = { viewModel.updateShowAll(it) },
                        icon = Icons.Default.Visibility
                    )
                }
            }
        }
    }
}
