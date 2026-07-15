package com.leaf.qrcodegenerator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.leaf.qrcodegenerator.ui.MiSansBoldFontFamily
import com.leaf.qrcodegenerator.ui.PageHorizontalPadding
import com.leaf.qrcodegenerator.ui.QrCodeGeneratorTheme
import com.leaf.qrcodegenerator.utils.ClipboardUtils
import com.leaf.qrcodegenerator.utils.SPUtils
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

class MainActivity : ComponentActivity() {
    private var history by mutableStateOf<List<String>>(emptyList())
    private var clipboardText by mutableStateOf("")
    private var showCameraPermissionDialog by mutableStateOf(false)

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) openScanner() else showCameraPermissionDialog = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshContent()
        setContent {
            QrCodeGeneratorTheme {
                MainScreen(
                    history = history,
                    clipboardText = clipboardText,
                    showCameraPermissionDialog = showCameraPermissionDialog,
                    onDismissPermissionDialog = { showCameraPermissionDialog = false },
                    onOpenSettings = ::openAppSettings,
                    onScan = ::requestCameraAndScan,
                    onGenerate = ::showQrCode,
                    onCopy = { ClipboardUtils.copyToClipboard(this, it) },
                    onClearHistory = {
                        SPUtils.clearHistory()
                        refreshContent()
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    private fun refreshContent() {
        history = SPUtils.getHistory()
        clipboardText = ClipboardUtils.getClipboardContent(this).trim()
    }

    private fun showQrCode(content: String) {
        SPUtils.saveHistory(content)
        history = SPUtils.getHistory()
        startActivity(Intent(this, QrCodeActivity::class.java).putExtra("content", content))
    }

    private fun requestCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openScanner()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openScanner() {
        startActivity(Intent(this, ScanActivity::class.java))
    }

    private fun openAppSettings() {
        showCameraPermissionDialog = false
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
    onClearHistory: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    val appTextStyles = MiuixTheme.textStyles
    val topAppBarTextStyles = remember(appTextStyles) {
        appTextStyles.copy(
            main = appTextStyles.main.copy(fontFamily = MiSansBoldFontFamily),
        )
    }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MiuixTheme(textStyles = topAppBarTextStyles) {
                TopAppBar(
                    title = if (pagerState.currentPage == 0) "生成" else "历史",
                    largeTitle = if (pagerState.currentPage == 0) "生成二维码" else "历史记录",
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(
                            onClick = if (pagerState.currentPage == 0) onScan else { { showClearDialog = true } },
                            modifier = Modifier.padding(end = PageHorizontalPadding),
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (pagerState.currentPage == 0) R.drawable.ic_scan else R.drawable.ic_delete,
                                ),
                                contentDescription = if (pagerState.currentPage == 0) {
                                    "扫描二维码"
                                } else {
                                    "清空历史记录"
                                },
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    icon = ImageVector.vectorResource(R.drawable.ic_generate),
                    label = "生成",
                )
                NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    icon = ImageVector.vectorResource(R.drawable.ic_history),
                    label = "历史",
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                if (page == 0) {
                    GeneratePage(
                        clipboardText = clipboardText,
                        onGenerate = onGenerate,
                        onEmptyContent = {
                            coroutineScope.launch { snackbarHostState.showSnackbar("内容不能为空") }
                        },
                    )
                } else {
                    HistoryPage(
                        history = history,
                        onOpen = onGenerate,
                        onCopy = {
                            onCopy(it)
                            coroutineScope.launch { snackbarHostState.showSnackbar("已复制到剪贴板") }
                        },
                    )
                }
            }

            SuperDialog(
                show = showClearDialog,
                title = "提示",
                summary = "确认清除历史记录？",
                onDismissRequest = { showClearDialog = false },
            ) {
                DialogActions(
                    confirmText = "确定",
                    onConfirm = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    onCancel = { showClearDialog = false },
                )
            }

            SuperDialog(
                show = showCameraPermissionDialog,
                title = "需要相机权限",
                summary = "请在系统设置中允许相机权限后再扫描二维码。",
                onDismissRequest = onDismissPermissionDialog,
            ) {
                DialogActions(
                    confirmText = "去设置",
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
    onGenerate: (String) -> Unit,
    onEmptyContent: () -> Unit,
) {
    var content by remember { mutableStateOf(TextFieldValue()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PageHorizontalPadding, vertical = 16.dp),
    ) {
        TextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier.fillMaxWidth().height(220.dp),
            label = "内容",
            minLines = 9,
            maxLines = 9,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val value = content.text.trim()
                if (value.isEmpty()) onEmptyContent() else onGenerate(value)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColorsPrimary(),
        ) {
            Text("生成二维码", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        }

        if (clipboardText.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = "剪贴板",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.footnote1,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = { onGenerate(clipboardText) },
            ) {
                Text(clipboardText, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun HistoryPage(
    history: List<String>,
    onOpen: (String) -> Unit,
    onCopy: (String) -> Unit,
) {
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.empty_history),
                contentDescription = "暂无历史记录",
                modifier = Modifier.size(width = 144.dp, height = 129.dp),
                contentScale = ContentScale.Fit,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = PageHorizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(history, key = { it }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                insideMargin = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                pressFeedbackType = PressFeedbackType.Sink,
                onClick = { onOpen(item) },
                onLongPress = { onCopy(item) },
            ) {
                Text(item, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(
            text = "取消",
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
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
            onClearHistory = {},
        )
    }
}

@Preview(name = "Generate Form", showBackground = true)
@Composable
private fun GeneratePagePreview() {
    QrCodeGeneratorTheme {
        GeneratePage(
            clipboardText = "https://compose-miuix-ui.github.io/miuix/",
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
            onOpen = {},
            onCopy = {},
        )
    }
}

@Preview(name = "History Empty", showBackground = true)
@Composable
private fun EmptyHistoryPagePreview() {
    QrCodeGeneratorTheme {
        HistoryPage(
            history = emptyList(),
            onOpen = {},
            onCopy = {},
        )
    }
}

@Preview(name = "Dialog Actions", showBackground = true)
@Composable
private fun DialogActionsPreview() {
    QrCodeGeneratorTheme {
        Box(Modifier.fillMaxWidth().padding(24.dp)) {
            DialogActions(
                confirmText = "确定",
                onConfirm = {},
                onCancel = {},
            )
        }
    }
}
