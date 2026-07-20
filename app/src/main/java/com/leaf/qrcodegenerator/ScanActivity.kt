package com.leaf.qrcodegenerator

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.leaf.qrcodegenerator.ui.PageHorizontalPadding
import com.leaf.qrcodegenerator.ui.AppSnackbarHost
import com.leaf.qrcodegenerator.ui.QrCodeGeneratorTheme
import com.leaf.qrcodegenerator.utils.ClipboardUtils
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

class ScanActivity : ComponentActivity() {
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
        .build()
    private val barcodeScanner: BarcodeScanner by lazy { BarcodeScanning.getClient(scannerOptions) }
    private val resultPending = AtomicBoolean(false)

    private var cameraProvider: ProcessCameraProvider? = null
    private var surfaceRequest by mutableStateOf<SurfaceRequest?>(null)
    private var scanResult by mutableStateOf<String?>(null)
    private var message by mutableStateOf<String?>(null)
    private var isPickingImage by mutableStateOf(false)

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            isPickingImage = false
            resultPending.set(false)
        } else {
            recognizeFromGallery(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QrCodeGeneratorTheme {
                ScanScreen(
                    surfaceRequest = surfaceRequest,
                    result = scanResult,
                    message = message,
                    isPickingImage = isPickingImage,
                    onMessageShown = { message = null },
                    onBack = ::finish,
                    onPickImage = ::pickImage,
                    onDismissResult = ::resumeCamera,
                    onCopyResult = {
                        scanResult?.let { ClipboardUtils.copyToClipboard(this, it) }
                        message = getString(R.string.common_copied_to_clipboard)
                        resumeCamera()
                    },
                )
            }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            message = getString(R.string.scan_camera_permission_missing)
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    cameraProvider = future.get()
                    bindCamera()
                } catch (_: Exception) {
                    message = getString(R.string.scan_camera_start_failed)
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        resultPending.set(false)
        provider.unbindAll()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request -> surfaceRequest = request }
        }
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    ContextCompat.getMainExecutor(this@ScanActivity),
                    MlKitAnalyzer(
                        listOf(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                        ContextCompat.getMainExecutor(this@ScanActivity),
                    ) { result ->
                        result?.getValue(barcodeScanner)
                            ?.firstNotNullOfOrNull { barcode ->
                                barcode.rawValue?.takeIf(String::isNotBlank)
                            }
                            ?.let(::onCameraResult)
                    },
                )
            }

        try {
            provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        } catch (_: Exception) {
            message = getString(R.string.scan_camera_connect_failed)
        }
    }

    private fun onCameraResult(content: String) {
        if (!resultPending.compareAndSet(false, true)) return
        cameraProvider?.unbindAll()
        surfaceRequest = null
        scanResult = content
    }

    private fun resumeCamera() {
        scanResult = null
        bindCamera()
    }

    private fun pickImage() {
        isPickingImage = true
        resultPending.set(true)
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    private fun recognizeFromGallery(uri: Uri) {
        val image = try {
            InputImage.fromFilePath(this, uri)
        } catch (_: Exception) {
            isPickingImage = false
            resultPending.set(false)
            message = getString(R.string.scan_image_read_failed)
            return
        }

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                val content = barcodes.firstNotNullOfOrNull { it.rawValue?.takeIf(String::isNotBlank) }
                if (content == null) {
                    resultPending.set(false)
                    message = getString(R.string.scan_image_no_qr_code)
                } else {
                    cameraProvider?.unbindAll()
                    surfaceRequest = null
                    resultPending.set(true)
                    scanResult = content
                }
            }
            .addOnFailureListener {
                resultPending.set(false)
                message = getString(R.string.scan_image_recognition_failed)
            }
            .addOnCompleteListener { isPickingImage = false }
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        barcodeScanner.close()
        super.onDestroy()
    }

}

@Composable
private fun ScanScreen(
    surfaceRequest: SurfaceRequest?,
    result: String?,
    message: String?,
    isPickingImage: Boolean,
    onMessageShown: () -> Unit,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onDismissResult: () -> Unit,
    onCopyResult: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            surfaceRequest?.let {
                CameraXViewfinder(
                    surfaceRequest = it,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = PageHorizontalPadding, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    backgroundColor = MiuixTheme.colorScheme.secondaryContainer,
                    minHeight = 35.dp,
                    minWidth = 35.dp,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_arrow_back_36),
                        contentDescription = stringResource(R.string.common_back),
                        modifier = Modifier.size(28.dp),
                    )
                }
                IconButton(
                    onClick = onPickImage,
                    enabled = !isPickingImage,
                    backgroundColor = MiuixTheme.colorScheme.secondaryContainer,
                    minHeight = 35.dp,
                    minWidth = 35.dp,
                ) {
                    if (isPickingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), size = 24.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_gallery),
                            contentDescription = stringResource(R.string.scan_from_gallery),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            SuperDialog(
                show = result != null,
                title = stringResource(R.string.scan_result),
                summary = result,
                onDismissRequest = onDismissResult,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = stringResource(R.string.common_cancel),
                        onClick = onDismissResult,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.size(16.dp))
                    TextButton(
                        text = stringResource(R.string.scan_copy_result),
                        onClick = onCopyResult,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }
}

@ComposePreview(name = "Scanner Result", showBackground = true)
@Composable
private fun ScanScreenPreview() {
    QrCodeGeneratorTheme {
        ScanScreen(
            surfaceRequest = null,
            result = "https://compose-miuix-ui.github.io/miuix/",
            message = null,
            isPickingImage = false,
            onMessageShown = {},
            onBack = {},
            onPickImage = {},
            onDismissResult = {},
            onCopyResult = {},
        )
    }
}

@ComposePreview(name = "Scanner - Picking Image", showBackground = true)
@Composable
private fun ScanPickingImagePreview() {
    QrCodeGeneratorTheme {
        ScanScreen(
            surfaceRequest = null,
            result = null,
            message = null,
            isPickingImage = true,
            onMessageShown = {},
            onBack = {},
            onPickImage = {},
            onDismissResult = {},
            onCopyResult = {},
        )
    }
}
