package com.leaf.qrcodegenerator

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dylanc.viewbinding.binding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kongzue.dialogx.dialogs.MessageDialog
import com.leaf.qrcodegenerator.databinding.ActivityScanBinding
import com.leaf.qrcodegenerator.utils.ClipboardUtils


class ScanActivity : AppCompatActivity() {
    private val binding: ActivityScanBinding by binding()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backContainer.setOnClickListener { finish() }
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                bindPreview(cameraProviderFuture.get())
            } catch (e: Exception) {

            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.cameraPv.surfaceProvider)
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this),
            QrCodeImageAnalyzer { barcode, _, _ ->
                cameraProvider.unbindAll()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!barcode.rawValue.isNullOrEmpty()) {
                        showResultDialog(barcode.rawValue!!)
                    } else {
                        MessageDialog.show("扫码结果", "扫码结果为空", "关闭")
                    }
                }, 500)
            })
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
    }

    private fun showResultDialog(content: String) =
        MessageDialog.show("扫码结果", content, "复制到剪贴板", "取消")
            .setOkButton { _, _ ->
                ClipboardUtils.copyToClipboard(this, content)
                bindPreview(cameraProviderFuture.get())
                false
            }

    private class QrCodeImageAnalyzer(val listener: (Barcode, Int, Int) -> Unit) :
        ImageAnalysis.Analyzer {
        //配置当前扫码格式
        private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()

        //获取解析器
        private val detector = BarcodeScanning.getClient(options)

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: kotlin.run {
                imageProxy.close()
                return
            }
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener {
                    if (it.isNotEmpty()) {
                        listener.invoke(it[0], imageProxy.width, imageProxy.height)
                    }
                }
                .addOnFailureListener {
                    // Failure
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}