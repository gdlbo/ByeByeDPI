package io.github.dovecoteescapee.byedpi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.ui.viewmodel.ProfilesViewModel
import io.github.dovecoteescapee.byedpi.utility.isTablet
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
    val isTablet = remember { context.isTablet() }
    
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = if (isTv || isTablet) 24.dp else 16.dp,
                    end = if (isTv || isTablet) 24.dp else 16.dp,
                    top = 16.dp,
                    bottom = 88.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.profiles_auto_switch),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    val wifiProfileName = viewModel.profiles.find { it.text == viewModel.wifiProfile }?.name
                        ?: if (viewModel.wifiProfile.isNotEmpty()) stringResource(R.string.profiles_unnamed) else stringResource(
                            R.string.profiles_none
                        )
                    
                    AutoSwitchCard(
                        title = stringResource(R.string.profiles_wifi),
                        profileName = wifiProfileName,
                        icon = Icons.Default.Wifi,
                        onClick = {
                            if (viewModel.profiles.isNotEmpty()) {
                                showProfileSelectionDialog =
                                    context.getString(R.string.profiles_wifi) to {
                                        viewModel.updateWifiProfile(
                                            it
                                        )
                                    }
                            }
                        },
                        onClear = if (viewModel.wifiProfile.isNotEmpty()) {
                            { viewModel.updateWifiProfile("") }
                        } else null
                    )
                }

                item {
                    val mobileProfileName = viewModel.profiles.find { it.text == viewModel.mobileProfile }?.name
                        ?: if (viewModel.mobileProfile.isNotEmpty()) stringResource(R.string.profiles_unnamed) else stringResource(
                            R.string.profiles_none
                        )

                    AutoSwitchCard(
                        title = stringResource(R.string.profiles_mobile),
                        profileName = mobileProfileName,
                        icon = Icons.Default.SignalCellularAlt,
                        onClick = {
                            if (viewModel.profiles.isNotEmpty()) {
                                showProfileSelectionDialog =
                                    context.getString(R.string.profiles_mobile) to {
                                        viewModel.updateMobileProfile(
                                            it
                                        )
                                    }
                            }
                        },
                        onClear = if (viewModel.mobileProfile.isNotEmpty()) {
                            { viewModel.updateMobileProfile("") }
                        } else null
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.profiles_list),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }

                if (viewModel.profiles.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
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
    }

    if (showAddDialog) {
        ProfileDialog(
            title = stringResource(R.string.profiles_add),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, command ->
                viewModel.addProfile(name.trim(), command.trim())
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
                viewModel.updateProfile(profile, name.trim(), command.trim())
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
        if (isTv || isTablet) {
            AlertDialog(
                onDismissRequest = { showProfileSelectionDialog = null },
                title = { Text(title) },
                text = {
                    LazyColumn(modifier = Modifier.widthIn(min = 400.dp)) {
                        item {
                            Surface(
                                onClick = {
                                    onSelect("")
                                    showProfileSelectionDialog = null
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(vertical = 2.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                ListItem(
                                    headlineContent = { Text(stringResource(R.string.clear_selection)) },
                                    leadingContent = { Icon(Icons.Default.Close, contentDescription = null) },
                                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                                )
                            }
                        }
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
                                    headlineContent = { Text(profile.name) },
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
                                    headlineContent = { Text(profile.name) },
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
fun AutoSwitchCard(
    viewModel: ProfilesViewModel = viewModel(),
    title: String,
    profileName: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (onClear != null && !isTv) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_selection)
                    )
                }
            }
            if (isTv && viewModel.profiles.isNotEmpty()) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ProfileItem(
    modifier: Modifier = Modifier,
    profile: Command,
    onApply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalIconButton(onClick = onApply) {
                    Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.apply))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
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
            Column(
                modifier = Modifier.widthIn(min = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                enabled = name.isNotBlank() && command.isNotBlank()
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
