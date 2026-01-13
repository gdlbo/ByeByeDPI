package io.github.dovecoteescapee.byedpi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.ui.components.PreferenceItem
import io.github.dovecoteescapee.byedpi.ui.components.SettingsCard
import io.github.dovecoteescapee.byedpi.ui.viewmodel.ProfilesViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel = viewModel(),
    onBack: () -> Unit,
    onNavigateToTest: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Command?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Command?>(null) }
    var showProfileSelectionDialog by remember { mutableStateOf<Pair<String, (String) -> Unit>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profiles_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.profiles_add))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.profiles_add))
            }
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
                SettingsCard(title = stringResource(R.string.profiles_auto_switch)) {
                    val wifiProfileName = viewModel.profiles.find { it.text == viewModel.wifiProfile }?.name 
                        ?: if (viewModel.wifiProfile.isNotEmpty()) stringResource(R.string.profiles_unnamed) else stringResource(R.string.profiles_none)
                    
                    val mobileProfileName = viewModel.profiles.find { it.text == viewModel.mobileProfile }?.name
                        ?: if (viewModel.mobileProfile.isNotEmpty()) stringResource(R.string.profiles_unnamed) else stringResource(R.string.profiles_none)

                    PreferenceItem(
                        title = stringResource(R.string.profiles_wifi),
                        summary = wifiProfileName,
                        icon = Icons.Default.Wifi,
                        onClick = {
                            showProfileSelectionDialog = context.getString(R.string.profiles_wifi) to { viewModel.updateWifiProfile(it) }
                        },
                        trailing = if (viewModel.wifiProfile.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.updateWifiProfile("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_selection))
                                }
                            }
                        } else null
                    )
                    
                    PreferenceItem(
                        title = stringResource(R.string.profiles_mobile),
                        summary = mobileProfileName,
                        icon = Icons.Default.SignalCellularAlt,
                        onClick = {
                            showProfileSelectionDialog = context.getString(R.string.profiles_mobile) to { viewModel.updateMobileProfile(it) }
                        },
                        trailing = if (viewModel.mobileProfile.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.updateMobileProfile("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_selection))
                                }
                            }
                        } else null
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.profiles_list),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }

            if (viewModel.profiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.profiles_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = onNavigateToTest,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.title_test))
                            }
                        }
                    }
                }
            } else {
                items(viewModel.profiles) { profile ->
                    ProfileItem(
                        profile = profile,
                        onApply = { viewModel.applyProfile(profile) },
                        onEdit = { showEditDialog = profile },
                        onDelete = { showDeleteDialog = profile }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProfileDialog(
            title = stringResource(R.string.profiles_add),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, command ->
                viewModel.addProfile(name, command)
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { profile ->
        ProfileDialog(
            title = stringResource(R.string.profiles_edit),
            initialName = profile.name ?: "",
            initialCommand = profile.text,
            onDismiss = { showEditDialog = null },
            onConfirm = { name, command ->
                viewModel.updateProfile(profile, name, command)
                showEditDialog = null
            }
        )
    }

    showDeleteDialog?.let { profile ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.profiles_delete_confirm)) },
            text = { Text(stringResource(R.string.profiles_delete_message, profile.name ?: profile.text)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProfile(profile)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
    
    showProfileSelectionDialog?.let { (title, onSelect) ->
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showProfileSelectionDialog = null },
                title = { Text(title) },
                text = {
                    LazyColumn {
                        items(viewModel.profiles) { profile ->
                            Surface(
                                onClick = {
                                    onSelect(profile.text)
                                    showProfileSelectionDialog = null
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(vertical = 2.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                ListItem(
                                    headlineContent = { Text(profile.name ?: stringResource(R.string.profiles_unnamed)) },
                                    supportingContent = { Text(profile.text, maxLines = 1) },
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileSelectionDialog = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showProfileSelectionDialog = null },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
                    LazyColumn {
                        items(viewModel.profiles) { profile ->
                            Surface(
                                onClick = {
                                    onSelect(profile.text)
                                    showProfileSelectionDialog = null
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                ListItem(
                                    headlineContent = { Text(profile.name ?: stringResource(R.string.profiles_unnamed)) },
                                    supportingContent = { Text(profile.text, maxLines = 1) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    profile: Command,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name ?: stringResource(R.string.profiles_unnamed),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = profile.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            IconButton(onClick = onApply) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.apply))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
fun ProfileDialog(
    title: String,
    initialName: String = "",
    initialCommand: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var command by remember { mutableStateOf(initialCommand) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profiles_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text(stringResource(R.string.profiles_command)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, command) },
                enabled = command.isNotBlank()
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
