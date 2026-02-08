package io.github.dovecoteescapee.byedpi.ui

import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600
    val useSplitLayout = isLandscape || isTablet
    val colorFloatingActionButton by animateColorAsState(
        if (viewModel.isTestingState) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.primaryContainer)


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

    BackHandler {
        if (viewModel.isTestingState) {
            viewModel.stopTesting()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            if (!useSplitLayout) {
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
            }
        },
        floatingActionButton = {
            if (!useSplitLayout) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (viewModel.isTestingState) viewModel.stopTesting() else viewModel.startTesting()
                    },
                    containerColor = colorFloatingActionButton,
                    contentColor = if (viewModel.isTestingState) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = {
                        Icon(
                            if (viewModel.isTestingState) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = if (viewModel.isTestingState) stringResource(R.string.test_stop) else stringResource(
                                R.string.test_start
                            )
                        )
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (useSplitLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = if (isTv) 48.dp else 24.dp, vertical = if (isTv) 24.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {
                            if (viewModel.isTestingState) viewModel.stopTesting()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                        Text(
                            text = stringResource(R.string.title_test),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = {
                            viewModel.performActionIfNotTesting(onOpenSettings)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    }

                    Button(
                        onClick = {
                            if (viewModel.isTestingState) viewModel.stopTesting() else viewModel.startTesting()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isTestingState) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (viewModel.isTestingState) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            if (viewModel.isTestingState) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.isTestingState) stringResource(R.string.test_stop) else stringResource(
                                R.string.test_start
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    TestStatusCard(viewModel = viewModel)
                    TestLogsSection(viewModel = viewModel)
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.test_results),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TestResultsList(viewModel = viewModel)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = viewModel.testHasEverRun,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                        TestStatusCard(viewModel = viewModel)
                    }
                }

                if (!viewModel.testHasEverRun) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TestLogsSection(
                    viewModel = viewModel,
                    compact = true,
                    titleContent = {
                        Text(
                            text = stringResource(R.string.test_results),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TestResultsList(viewModel = viewModel, contentPadding = PaddingValues(bottom = 88.dp))
            }
        }
    }

    CommandActionSheet(viewModel = viewModel, isTv = isTv)
}

@Composable
fun TestStatusCard(viewModel: TestViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.test_status),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(if (viewModel.isTestingState) R.string.test_process else R.string.test_complete),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            AnimatedVisibility(visible = viewModel.isTestingState) {
                Column() {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Column() {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.test_process_strategy),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${viewModel.checkedCmdCount}/${viewModel.totalCmdCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            }
                            LinearProgressIndicator(
                                progress = { viewModel.overallProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.2f
                                ),
                            )
                        }
                        Column() {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stringResource(R.string.test_process_domain),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${viewModel.checkedSitesCount}/${viewModel.totalSitesCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            }
                            LinearProgressIndicator(
                                progress = { viewModel.currentStrategyProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                    alpha = 0.2f
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestLogsSection(
    viewModel: TestViewModel,
    compact: Boolean = false,
    titleContent: @Composable (() -> Unit)? = null
) {
    var showLogs by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val logHeight = if (compact) 200.dp else (configuration.screenHeightDp.dp / 2).coerceIn(200.dp, 600.dp)

    Column {
        if (compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    titleContent?.invoke()
                }
                TextButton(onClick = { showLogs = !showLogs }) {
                    Text(if (showLogs) stringResource(R.string.test_hide_logs) else stringResource(R.string.test_show_logs))
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.test_show_logs),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(checked = showLogs, onCheckedChange = { showLogs = it })
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        AnimatedVisibility(
            visible = showLogs,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(logHeight)
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
    }
}

@Composable
fun TestResultsList(
    viewModel: TestViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
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
                    onSave = { viewModel.saveProfile(result.command, "") },
                    isTv = isTv
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandActionSheet(viewModel: TestViewModel, isTv: Boolean) {
    viewModel.showCommandSheet?.let { command ->
        if (isTv) {
            val result = remember(command, viewModel.testResults) {
                viewModel.testResults.find { it.command == command }
            }

            AlertDialog(
                onDismissRequest = { viewModel.showCommandSheet = null },
                title = { Text(stringResource(R.string.cmd_history_menu)) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (result != null) {
                            Text(
                                text = "${result.successCount}/${result.total} (${result.percentage}%)",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (result.percentage >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )

                            val failedSites = result.siteResults.filter { it.successCount == 0 }
                            if (failedSites.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.test_details),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                failedSites.take(3).forEach { site ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = site.domain,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (failedSites.size > 3) {
                                    Text(
                                        text = "... ${failedSites.size - 3} more",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }

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
    icon: ImageVector,
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
    onSave: () -> Unit,
    isTv: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var cardSize by remember { mutableIntStateOf(0) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                // expand maybe idk...
            }
            .scale(if (isFocused) 1.02f else 1f)
            .clip(shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(radius = cardSize.dp),
                onClick = {
                    if (isTv) {
                        onMore()
                    } else {
                        expanded = !expanded
                    }
                }
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
            .then(
                if (isFocused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier
            )
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
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.successCount}/${result.total} (${result.percentage}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.percentage >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                if (!isTv) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onMore) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                    }
                }
            }
            if (!isTv) {
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
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
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
}
