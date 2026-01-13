package io.github.dovecoteescapee.byedpi.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.ui.viewmodel.MainViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onPrepareVpn: (android.content.Intent) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveLogs: () -> Unit,
    onCloseApp: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    val status = viewModel.currentStatus
    val mode = viewModel.currentMode
    val preferredMode = viewModel.preferredMode
    val (ip, port) = viewModel.proxyAddress
    val profileName = viewModel.currentProfileName
    
    var showMenu by remember { mutableStateOf(false) }
    val isRunning = status == AppStatus.Running

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { messageResId ->
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.save_logs)) },
                            leadingIcon = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onSaveLogs()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.close_app)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onCloseApp()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isTv) 48.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isTv) {
                    TvLayout(
                        isRunning = isRunning,
                        status = status,
                        mode = mode,
                        preferredMode = preferredMode,
                        ip = ip,
                        port = port,
                        profileName = profileName,
                        isClickable = viewModel.isClickable,
                        isCmdEnabled = viewModel.isCmdEnabled,
                        onToggle = { viewModel.toggleService(onPrepareVpn) },
                        onSetMode = { viewModel.setMode(it) },
                        onOpenEditor = { viewModel.performActionIfStopped(onOpenEditor) },
                        onOpenSettings = { viewModel.performActionIfStopped(onOpenSettings) },
                        onSaveLogs = onSaveLogs,
                        onOpenProfiles = { viewModel.performActionIfStopped(onOpenProfiles) }
                    )
                } else {
                    MobileLayout(
                        minHeight = minHeight,
                        isRunning = isRunning,
                        status = status,
                        mode = mode,
                        preferredMode = preferredMode,
                        ip = ip,
                        port = port,
                        profileName = profileName,
                        isClickable = viewModel.isClickable,
                        isCmdEnabled = viewModel.isCmdEnabled,
                        onToggle = { viewModel.toggleService(onPrepareVpn) },
                        onSetMode = { viewModel.setMode(it) },
                        onOpenEditor = { viewModel.performActionIfStopped(onOpenEditor) },
                        onOpenSettings = { viewModel.performActionIfStopped(onOpenSettings) },
                        onSaveLogs = onSaveLogs,
                        onOpenProfiles = { viewModel.performActionIfStopped(onOpenProfiles) }
                    )
                }
            }
        }
    }
}

@Composable
fun MobileLayout(
    minHeight: androidx.compose.ui.unit.Dp,
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    isCmdEnabled: Boolean,
    onToggle: () -> Unit,
    onSetMode: (Mode) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveLogs: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Column(
        modifier = Modifier.heightIn(min = minHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Status Indicator
        StatusButton(isRunning, isClickable, onToggle)

        Spacer(modifier = Modifier.height(32.dp))

        // Status Text
        StatusText(status, isRunning, mode, preferredMode, ip, port, profileName)

        Spacer(modifier = Modifier.weight(1f))

        // Quick Actions Card
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f).alpha(if (isRunning) 0.5f else 1f),
                    icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                    label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                    onClick = { 
                        val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                        onSetMode(newMode)
                    }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f).alpha(if (isRunning) 0.5f else 1f),
                    icon = Icons.AutoMirrored.Filled.List,
                    label = stringResource(R.string.profiles_title),
                    onClick = onOpenProfiles
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f).alpha(if (isRunning) 0.5f else 1f),
                    icon = if (isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                    label = stringResource(R.string.editor),
                    onClick = onOpenEditor
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f).alpha(if (isRunning) 0.5f else 1f),
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    onClick = onOpenSettings
                )
            }
        }
    }
}

@Composable
fun TvLayout(
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    isCmdEnabled: Boolean,
    onToggle: () -> Unit,
    onSetMode: (Mode) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveLogs: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp)
        ) {
            StatusButton(isRunning, isClickable, onToggle)
            Spacer(modifier = Modifier.height(32.dp))
            StatusText(status, isRunning, mode, preferredMode, ip, port, profileName)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonModifier = Modifier.fillMaxWidth()
            
            TvActionButton(
                icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                onClick = { 
                    val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                    onSetMode(newMode)
                },
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = Icons.AutoMirrored.Filled.List,
                label = stringResource(R.string.profiles_title),
                onClick = onOpenProfiles,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = if (isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                label = stringResource(R.string.editor),
                onClick = onOpenEditor,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.settings),
                onClick = onOpenSettings,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
        }
    }
}

@Composable
fun StatusButton(isRunning: Boolean, isClickable: Boolean, onToggle: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        val buttonScale by animateFloatAsState(
            targetValue = if (isRunning) 1.1f else 1f,
            animationSpec = tween(500), 
            label = "scale"
        )
        
        val buttonColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            animationSpec = tween(500),
            label = "color"
        )

        val iconColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(500),
            label = "iconColor"
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(buttonColor)
                .onFocusChanged { isFocused = it.isFocused }
                .scale(if (isFocused) 1.1f else 1f)
                .border(if (isFocused) 4.dp else 0.dp, if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                .clickable(
                    enabled = isClickable,
                    onClick = onToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = iconColor
            )
        }
    }
}

@Composable
fun StatusText(
    status: AppStatus,
    isRunning: Boolean,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?
) {
    val statusTextRes = when (status) {
        AppStatus.Halted -> if (preferredMode == Mode.VPN) R.string.vpn_disconnected else R.string.proxy_down
        AppStatus.Running -> if (mode == Mode.VPN) R.string.vpn_connected else R.string.proxy_up
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(statusTextRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        if (profileName != null) {
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (isRunning && mode == Mode.Proxy) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.proxy_address, ip, port),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TvActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .scale(if (isFocused) 1.05f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
