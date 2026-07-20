package com.leaf.qrcodegenerator

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.leaf.qrcodegenerator.ui.PageHorizontalPadding
import com.leaf.qrcodegenerator.ui.AppSnackbarHost
import com.leaf.qrcodegenerator.ui.QrCodeGeneratorTheme
import com.leaf.qrcodegenerator.utils.createQrCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

class QrCodeActivity : ComponentActivity() {
    private lateinit var content: String
    private var saveMessage by mutableStateOf<String?>(null)

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) saveQrCode() else saveMessage = getString(R.string.qr_code_storage_permission_missing)
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
                QrCodeScreen(
                    content = content,
                    message = saveMessage,
                    onMessageShown = { saveMessage = null },
                    onBack = ::finish,
                    onSave = ::requestStorageAndSave,
                )
            }
        }
    }

    private fun requestStorageAndSave() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            saveQrCode()
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun saveQrCode() {
        lifecycleScope.launch {
            saveMessage = if (savePhoto()) {
                getString(R.string.qr_code_save_success)
            } else {
                getString(R.string.qr_code_save_failed)
            }
        }
    }

    private suspend fun savePhoto(): Boolean = withContext(Dispatchers.IO) {
        val bitmap = createQrCode(content, 800)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/QRCodeGenerator")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        var saveUri: Uri? = null
        try {
            saveUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false
            val saved = contentResolver.openOutputStream(saveUri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
            if (!saved) {
                contentResolver.delete(saveUri, null, null)
                return@withContext false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.update(
                    saveUri,
                    ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                    null,
                    null,
                )
            }
            true
        } catch (_: Exception) {
            saveUri?.let { contentResolver.delete(it, null, null) }
            false
        }
    }
}

@Composable
private fun QrCodeScreen(
    content: String,
    message: String?,
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
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = stringResource(R.string.qr_code_title),
                largeTitle = stringResource(R.string.qr_code_title),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.padding(start = PageHorizontalPadding)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_arrow_back_36),
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(stringResource(R.string.qr_code_save), fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                }
            }
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }
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
            onMessageShown = {},
            onBack = {},
            onSave = {},
        )
    }
}
