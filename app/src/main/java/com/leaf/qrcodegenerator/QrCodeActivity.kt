package com.leaf.qrcodegenerator

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hi.dhl.binding.viewbind
import com.king.zxing.util.CodeUtils
import com.leaf.qrcodegenerator.databinding.ActivityQrCodeBinding
import com.leaf.qrcodegenerator.utils.StatusBarUtil
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class QrCodeActivity : AppCompatActivity() {
    private val binding: ActivityQrCodeBinding by viewbind()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.fitSystemBar(this)
        setContentView(binding.root)
        binding.statusBarFix.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(this)
        )
        binding.FLBack.setOnClickListener { finish() }
        binding.ivQrCode.setImageBitmap(
            CodeUtils.createQRCode(
                intent.getStringExtra("content"),
                800
            )
        )
        binding.btnSave.setOnClickListener {
            saveQRCode()
        }
        if (intent.getStringExtra("content").isNullOrEmpty()) {
            finish()
        }
    }

    private fun saveQRCode() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList, _ ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "二维码生成器 需要您同意以下权限才能保存二维码",
                    "确定",
                    "取消"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "您需要去应用程序设置当中手动开启权限",
                    "确定"
                )
            }
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    lifecycleScope.launch { savePhoto() }
                } else {
                    Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    //协程处理保存图片
    private suspend fun savePhoto() {
        withContext(Dispatchers.IO) {
            val bitmap: Bitmap = CodeUtils.createQRCode(
                intent.getStringExtra("content"),
                800
            )
            //设置保存地址
            val content = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_${Calendar.getInstance().timeInMillis}")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
            val saveUri: Uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                content
            ) ?: kotlin.run {
                Toast.makeText(this@QrCodeActivity, "保存失败", Toast.LENGTH_SHORT).show()
                return@withContext
            }
            //保存图片
            contentResolver.openOutputStream(saveUri).use {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)) { //90%的压缩率，it是输出流
                    //在主线程提示用户
                    MainScope().launch {
                        Toast.makeText(
                            this@QrCodeActivity,
                            "保存成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(
                            this@QrCodeActivity,
                            "保存失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}