package com.leaf.qrcodegenerator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leaf.qrcodegenerator.ui.PageHorizontalPadding
import com.leaf.qrcodegenerator.ui.AppSnackbarHost
import com.leaf.qrcodegenerator.ui.QrCodeGeneratorTheme
import com.leaf.qrcodegenerator.utils.createQrCode
import com.leaf.qrcodegenerator.viewmodel.QrCodeViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

class QrCodeActivity : ComponentActivity() {
    private lateinit var content: String
    private val viewModel: QrCodeViewModel by viewModels {
        QrCodeViewModel.factory(applicationContext)
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) saveQrCode() else viewModel.showStoragePermissionMissing()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = intent.getStringExtra("content")?.trim().orEmpty()
        if (content.isEmpty()) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            QrCodeGeneratorTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                QrCodeScreen(
                    content = content,
                    message = uiState.messageRes?.let(::getString),
                    isSaving = uiState.isSaving,
                    onMessageShown = viewModel::consumeMessage,
                    onBack = ::finish,
                    onSave = ::requestStorageAndSave,
                )
            }
        }
    }

    private fun requestStorageAndSave() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            saveQrCode()
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun saveQrCode() {
        viewModel.save(content)
    }
}

@Composable
private fun QrCodeScreen(
    content: String,
    message: String?,
    isSaving: Boolean,
    onMessageShown: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    val foregroundColor = MiuixTheme.colorScheme.onSurface.toArgb()
    val bitmap = remember(content, foregroundColor) {
        createQrCode(
            content = content,
            size = 800,
            foregroundColor = foregroundColor,
        ).asImageBitmap()
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = stringResource(R.string.qr_code_title),
                largeTitle = stringResource(R.string.qr_code_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = PageHorizontalPadding)
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(R.string.common_back),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { Spacer(Modifier.height(64.dp)) }
            item {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.qr_code_image_description),
                    modifier = Modifier.size(280.dp),
                )
            }
            item { Spacer(Modifier.height(32.dp)) }
            item {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PageHorizontalPadding),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        stringResource(R.string.qr_code_save),
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                }
            }
            item { Spacer(Modifier
                .height(24.dp)
                .navigationBarsPadding()) }
        }
    }
}

@Preview(name = "QR Code", showBackground = true)
@Composable
private fun QrCodeScreenPreview() {
    QrCodeGeneratorTheme {
        QrCodeScreen(
            content = "https://compose-miuix-ui.github.io/miuix/",
            message = null,
            isSaving = false,
            onMessageShown = {},
            onBack = {},
            onSave = {},
        )
    }
}
