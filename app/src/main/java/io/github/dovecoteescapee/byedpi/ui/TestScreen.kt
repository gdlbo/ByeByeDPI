package io.github.dovecoteescapee.byedpi.ui

import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.activities.MainActivity
import io.github.dovecoteescapee.byedpi.ui.viewmodel.TestResult
import io.github.dovecoteescapee.byedpi.ui.viewmodel.TestViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TestScreen(
    viewModel: TestViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val isTv = remember { context.isTv() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { messageResId ->
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(viewModel.isTestingState) {
        if (viewModel.isTestingState) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_test)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.isTestingState) viewModel.stopTesting()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.performActionIfNotTesting(onOpenSettings)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (viewModel.isTestingState) viewModel.stopTesting() else viewModel.startTesting()
                },
                containerColor = if (viewModel.isTestingState) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (viewModel.isTestingState) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        if (viewModel.isTestingState) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                },
                text = { 
                    Text(
                        text = if (viewModel.isTestingState) stringResource(R.string.test_stop) else stringResource(R.string.test_start)
                    )
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = if (isTv) 48.dp else 16.dp)
        ) {
            AnimatedVisibility(
                visible = viewModel.progressText.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically()
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.test_status),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = viewModel.progressText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            if (viewModel.progressText.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            var showLogs by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.test_results),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showLogs = !showLogs }) {
                    Text(if (showLogs) stringResource(R.string.test_hide_logs) else stringResource(R.string.test_show_logs))
                }
            }

            AnimatedVisibility(
                visible = showLogs,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            ClickableText(
                                text = viewModel.resultsLog,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Monospace
                                ),
                                onClick = { offset ->
                                    viewModel.resultsLog.getStringAnnotations(tag = "COMMAND", start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            viewModel.showCommandSheet = annotation.item
                                        }
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.testResults,
                    key = { it.command }
                ) { result ->
                    Box(
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        TestResultCard(
                            result = result,
                            onApply = { viewModel.applyCommand(result.command) },
                            onCopy = { viewModel.copyCommand(result.command) },
                            onMore = { viewModel.showCommandSheet = result.command },
                            onSave = { viewModel.saveProfile(result.command, "") }
                        )
                    }
                }
            }
        }
    }

    viewModel.showCommandSheet?.let { command ->
        if (isTv) {
            AlertDialog(
                onDismissRequest = { viewModel.showCommandSheet = null },
                title = { Text(stringResource(R.string.cmd_history_menu)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TvDialogButton(
                            text = stringResource(R.string.cmd_history_apply),
                            icon = Icons.Default.Terminal,
                            onClick = {
                                viewModel.applyCommand(command)
                                viewModel.showCommandSheet = null
                            }
                        )
                        TvDialogButton(
                            text = stringResource(R.string.cmd_history_copy),
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                viewModel.copyCommand(command)
                                viewModel.showCommandSheet = null
                            }
                        )
                        TvDialogButton(
                            text = stringResource(R.string.profiles_add),
                            icon = Icons.Default.Add,
                            onClick = {
                                viewModel.saveProfile(command, "")
                                viewModel.showCommandSheet = null
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.showCommandSheet = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showCommandSheet = null },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = stringResource(R.string.cmd_history_menu),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
                    Surface(
                        onClick = {
                            viewModel.applyCommand(command)
                            viewModel.showCommandSheet = null
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cmd_history_apply)) },
                            leadingContent = { Icon(Icons.Default.Terminal, contentDescription = null) }
                        )
                    }
                    Surface(
                        onClick = {
                            viewModel.copyCommand(command)
                            viewModel.showCommandSheet = null
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cmd_history_copy)) },
                            leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                        )
                    }
                    Surface(
                        onClick = {
                            viewModel.saveProfile(command, "")
                            viewModel.showCommandSheet = null
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.profiles_add)) },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvDialogButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .scale(if (isFocused) 1.05f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (isFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text)
        }
    }
}

@Composable
fun TestResultCard(
    result: TestResult,
    onApply: () -> Unit,
    onCopy: () -> Unit,
    onMore: () -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    var cardSize by remember { mutableStateOf(0) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(radius = cardSize.dp),
                onClick = { expanded = !expanded }
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val size = if (placeable.width > placeable.height) placeable.width else placeable.height

                if (cardSize != size) {
                    cardSize = size
                }

                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.command,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.successCount}/${result.total} (${result.percentage}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.percentage >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMore) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.test_details),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val failedSites = result.siteResults.filter { it.successCount == 0 }
                    
                    if (failedSites.isEmpty()) {
                        Text(
                            text = stringResource(R.string.test_all_connected),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        failedSites.forEach { site ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = site.domain,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.test_not_connected),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSave,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.add)
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = onApply,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.cmd_history_apply)
                            )
                        }
                    }
                }
            }
        }
    }
}
