package com.leaf.qrcodegenerator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leaf.qrcodegenerator.ui.AppSnackbarHost
import com.leaf.qrcodegenerator.ui.PageHorizontalPadding
import com.leaf.qrcodegenerator.ui.QrCodeGeneratorTheme
import com.leaf.qrcodegenerator.utils.ClipboardUtils
import com.leaf.qrcodegenerator.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) openScanner() else viewModel.showCameraPermissionDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QrCodeGeneratorTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MainScreen(
                    history = uiState.history,
                    clipboardText = uiState.clipboardText,
                    showCameraPermissionDialog = uiState.showCameraPermissionDialog,
                    onDismissPermissionDialog = viewModel::dismissCameraPermissionDialog,
                    onOpenSettings = ::openAppSettings,
                    onScan = ::requestCameraAndScan,
                    onGenerate = ::showQrCode,
                    onCopy = { ClipboardUtils.copyToClipboard(this, it) },
                    onDeleteHistory = viewModel::deleteHistory,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHistory()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            viewModel.updateClipboard(ClipboardUtils.getClipboardContent(this))
        }
    }

    private fun showQrCode(content: String) {
        viewModel.saveHistory(content)
        startActivity(Intent(this, QrCodeActivity::class.java).putExtra("content", content))
    }

    private fun requestCameraAndScan() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openScanner()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openScanner() {
        startActivity(Intent(this, ScanActivity::class.java))
    }

    private fun openAppSettings() {
        viewModel.dismissCameraPermissionDialog()
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            },
        )
    }
}

@Composable
private fun MainScreen(
    history: List<String>,
    clipboardText: String,
    showCameraPermissionDialog: Boolean,
    onDismissPermissionDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onScan: () -> Unit,
    onGenerate: (String) -> Unit,
    onCopy: (String) -> Unit,
    onDeleteHistory: (Set<String>) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    val generateTitle = stringResource(R.string.main_generate)
    val historyTitle = stringResource(R.string.main_history)
    val emptyContentMessage = stringResource(R.string.main_empty_content)
    val copiedMessage = stringResource(R.string.common_copied_to_clipboard)
    var isHistorySelectionMode by remember { mutableStateOf(false) }
    var selectedHistoryItems by remember { mutableStateOf(emptySet<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val exitHistorySelection = {
        isHistorySelectionMode = false
        selectedHistoryItems = emptySet()
    }

    BackHandler(enabled = isHistorySelectionMode && pagerState.currentPage == 1) {
        exitHistorySelection()
    }

    LaunchedEffect(history) {
        selectedHistoryItems = selectedHistoryItems.intersect(history.toSet())
        if (history.isEmpty()) exitHistorySelection()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 1) exitHistorySelection()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MiuixTheme {
                TopAppBar(
                    title = when {
                    pagerState.currentPage == 0 -> generateTitle
                    isHistorySelectionMode -> stringResource(
                        R.string.main_selected_history_count,
                        selectedHistoryItems.size,
                    )

                    else -> historyTitle
                }, largeTitle = when {
                    pagerState.currentPage == 0 -> {
                        stringResource(R.string.main_generate_qr_code)
                    }

                    isHistorySelectionMode -> stringResource(
                        R.string.main_selected_history_count,
                        selectedHistoryItems.size,
                    )

                    else -> {
                        stringResource(R.string.main_history_records)
                    }
                }, scrollBehavior = scrollBehavior, navigationIcon = {
                    if (isHistorySelectionMode && pagerState.currentPage == 1) {
                        IconButton(
                            onClick = exitHistorySelection,
                            modifier = Modifier.padding(start = PageHorizontalPadding),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Close,
                                contentDescription = stringResource(R.string.common_cancel),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }, actions = {
                    if (isHistorySelectionMode && pagerState.currentPage == 1) {
                        val allSelected = selectedHistoryItems.size == history.size
                        val selectAllDescription = stringResource(R.string.common_select_all)
                        Checkbox(
                            state = if (allSelected) ToggleableState.On else ToggleableState.Off,
                            onClick = {
                                selectedHistoryItems =
                                    if (allSelected) emptySet() else history.toSet()
                            },
                            modifier = Modifier
                                .padding(end = PageHorizontalPadding)
                                .semantics {
                                    contentDescription = selectAllDescription
                                },
                        )
                    } else {
                        IconButton(
                            onClick = if (pagerState.currentPage == 0) onScan else {
                                { if (history.isNotEmpty()) isHistorySelectionMode = true }
                            },
                            enabled = pagerState.currentPage == 0 || history.isNotEmpty(),
                            modifier = Modifier.padding(end = PageHorizontalPadding),
                        ) {
                            Icon(
                                imageVector = if (pagerState.currentPage == 0) {
                                    MiuixIcons.Scan
                                } else {
                                    MiuixIcons.Delete
                                },
                                contentDescription = if (pagerState.currentPage == 0) {
                                    stringResource(R.string.main_scan_qr_code)
                                } else {
                                    stringResource(R.string.main_select_history_to_delete)
                                },
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                })
            }
        },
        bottomBar = {
            NavigationBar {
                if (isHistorySelectionMode && pagerState.currentPage == 1) {
                    NavigationBarItem(
                        modifier = Modifier.weight(1f),
                        selected = true,
                        onClick = { showDeleteDialog = true },
                        enabled = selectedHistoryItems.isNotEmpty(),
                        icon = MiuixIcons.Delete,
                        label = stringResource(R.string.main_delete_selected_history_count),
                    )
                } else {
                    NavigationBarItem(
                        modifier = Modifier.weight(1f),
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            exitHistorySelection()
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        },
                        icon = ImageVector.vectorResource(R.drawable.ic_generate),
                        label = generateTitle,
                    )
                    NavigationBarItem(
                        modifier = Modifier.weight(1f),
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        icon = MiuixIcons.Recent,
                        label = historyTitle,
                    )
                }
            }
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                if (page == 0) {
                    GeneratePage(
                        clipboardText = clipboardText,
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        scrollBehavior = scrollBehavior,
                        onGenerate = onGenerate,
                        onEmptyContent = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(emptyContentMessage)
                            }
                        },
                    )
                } else {
                    HistoryPage(
                        history = history,
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        scrollBehavior = scrollBehavior,
                        onOpen = onGenerate,
                        onCopy = {
                            onCopy(it)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(copiedMessage)
                            }
                        },
                        selectionMode = isHistorySelectionMode,
                        selectedItems = selectedHistoryItems,
                        onToggleSelection = { item ->
                            selectedHistoryItems = if (item in selectedHistoryItems) {
                                selectedHistoryItems - item
                            } else {
                                selectedHistoryItems + item
                            }
                        },
                    )
                }
            }

            SuperDialog(
                show = showDeleteDialog,
                title = stringResource(R.string.common_notice),
                summary = stringResource(
                    R.string.main_confirm_delete_selected_history,
                    selectedHistoryItems.size,
                ),
                onDismissRequest = { showDeleteDialog = false },
            ) {
                DialogActions(
                    confirmText = stringResource(R.string.common_confirm),
                    onConfirm = {
                        onDeleteHistory(selectedHistoryItems)
                        showDeleteDialog = false
                        exitHistorySelection()
                    },
                    onCancel = { showDeleteDialog = false },
                )
            }

            SuperDialog(
                show = showCameraPermissionDialog,
                title = stringResource(R.string.main_camera_permission_required),
                summary = stringResource(R.string.main_camera_permission_summary),
                onDismissRequest = onDismissPermissionDialog,
            ) {
                DialogActions(
                    confirmText = stringResource(R.string.main_open_settings),
                    onConfirm = onOpenSettings,
                    onCancel = onDismissPermissionDialog,
                )
            }
        }
    }
}

@Composable
private fun GeneratePage(
    clipboardText: String,
    bottomPadding: androidx.compose.ui.unit.Dp,
    scrollBehavior: ScrollBehavior,
    onGenerate: (String) -> Unit,
    onEmptyContent: () -> Unit,
) {
    var content by remember { mutableStateOf(TextFieldValue()) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .scrollEndHaptic()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = PageHorizontalPadding,
            end = PageHorizontalPadding,
            bottom = bottomPadding,
        ),
    ) {
        item { Spacer(Modifier.height(12.dp)) }
        item {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = stringResource(R.string.main_content_label),
                minLines = 9,
                maxLines = 9,
            )
        }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            Button(
                onClick = {
                    val value = content.text.trim()
                    if (value.isEmpty()) onEmptyContent() else onGenerate(value)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColorsPrimary(),
            ) {
                Text(
                    stringResource(R.string.main_generate_qr_code),
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )
            }
        }

        if (clipboardText.isNotEmpty()) {
            item { Spacer(Modifier.height(32.dp)) }
            item {
                Text(
                    text = stringResource(R.string.main_clipboard),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.footnote1,
                    fontWeight = FontWeight.Medium,
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    insideMargin = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                    pressFeedbackType = PressFeedbackType.Sink,
                    onClick = { onGenerate(clipboardText) },
                ) {
                    Text(
                        clipboardText,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun HistoryPage(
    history: List<String>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    scrollBehavior: ScrollBehavior,
    onOpen: (String) -> Unit,
    onCopy: (String) -> Unit,
    selectionMode: Boolean,
    selectedItems: Set<String>,
    onToggleSelection: (String) -> Unit,
) {
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.empty_history),
                contentDescription = stringResource(R.string.main_empty_history),
                modifier = Modifier.size(width = 144.dp, height = 129.dp),
                contentScale = ContentScale.Fit,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .scrollEndHaptic()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            start = PageHorizontalPadding,
            end = PageHorizontalPadding,
            bottom = bottomPadding,
        ),
    ) {
        item { Spacer(Modifier.height(12.dp)) }
        itemsIndexed(history, key = { index, item -> "$index:$item" }) { _, item ->
            val isSelected = item in selectedItems
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = {
                    if (selectionMode) onToggleSelection(item) else onOpen(item)
                },
                onLongPress = if (selectionMode) null else {
                    { onCopy(item) }
                },
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                    )
                    if (selectionMode) {
                        Spacer(Modifier.width(12.dp))
                        Checkbox(
                            state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                            onClick = { onToggleSelection(item) },
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun DialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            text = stringResource(R.string.common_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            text = confirmText,
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
    }
}

@Preview(name = "Main - Generate", showBackground = true)
@Composable
private fun MainScreenPreview() {
    QrCodeGeneratorTheme {
        MainScreen(
            history = listOf("https://example.com", "Compose Miuix"),
            clipboardText = "https://compose-miuix-ui.github.io/miuix/",
            showCameraPermissionDialog = false,
            onDismissPermissionDialog = {},
            onOpenSettings = {},
            onScan = {},
            onGenerate = {},
            onCopy = {},
            onDeleteHistory = {},
        )
    }
}

@Preview(name = "Generate Form", showBackground = true)
@Composable
private fun GeneratePagePreview() {
    QrCodeGeneratorTheme {
        GeneratePage(
            clipboardText = "https://compose-miuix-ui.github.io/miuix/",
            bottomPadding = 0.dp,
            scrollBehavior = MiuixScrollBehavior(),
            onGenerate = {},
            onEmptyContent = {},
        )
    }
}

@Preview(name = "History List", showBackground = true)
@Composable
private fun HistoryPagePreview() {
    QrCodeGeneratorTheme {
        HistoryPage(
            history = listOf(
                "https://compose-miuix-ui.github.io/miuix/",
                "Jetpack Compose 可预览界面",
                "https://developer.android.com/jetpack/compose",
            ),
            bottomPadding = 0.dp,
            scrollBehavior = MiuixScrollBehavior(),
            onOpen = {},
            onCopy = {},
            selectionMode = true,
            selectedItems = setOf("Jetpack Compose 可预览界面"),
            onToggleSelection = {},
        )
    }
}

@Preview(name = "History Empty", showBackground = true)
@Composable
private fun EmptyHistoryPagePreview() {
    QrCodeGeneratorTheme {
        HistoryPage(
            history = emptyList(),
            bottomPadding = 0.dp,
            scrollBehavior = MiuixScrollBehavior(),
            onOpen = {},
            onCopy = {},
            selectionMode = false,
            selectedItems = emptySet(),
            onToggleSelection = {},
        )
    }
}

@Preview(name = "Dialog Actions", showBackground = true)
@Composable
private fun DialogActionsPreview() {
    QrCodeGeneratorTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            DialogActions(
                confirmText = "确定",
                onConfirm = {},
                onCancel = {},
            )
        }
    }
}
