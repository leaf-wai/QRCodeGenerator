package com.leaf.qrcodegenerator

import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hi.dhl.binding.viewbind
import com.leaf.qrcodegenerator.databinding.ActivityMainBinding
import com.leaf.qrcodegenerator.utils.StatusBarUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewbind()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.fitSystemBar(this)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        binding.statusBarFix.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(this)
        )
        binding.btnGenerate.setOnClickListener {
            if (binding.etContent.text?.trim()?.isEmpty() == true){
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT)
                    .show()
            } else {
                startActivity(Intent(this, QrCodeActivity::class.java).apply {
                    putExtra("content", binding.etContent.text?.trim())
                })
            }
        }
        binding.cvCb.setOnClickListener {
            startActivity(Intent(this, QrCodeActivity::class.java).apply {
                putExtra("content", binding.tvCb.text.trim())
            })
        }
    }

    private fun setClipboard() {
        if (getClipboardContent() != "") {
            binding.tvCb.text = getClipboardContent()
            binding.LLClipboard.visibility = View.VISIBLE
        } else
            binding.LLClipboard.visibility = View.GONE
    }

    private fun getClipboardContent(): String {
        val clipboard = this@MainActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData == null || clipData.itemCount <= 0) {
            return ""
        }
        val item = clipData.getItemAt(0)
        return if (item == null || item.text == null) {
            ""
        } else item.text.toString()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            setClipboard()
        }
    }
}