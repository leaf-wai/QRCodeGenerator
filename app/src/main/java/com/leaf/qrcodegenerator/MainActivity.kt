package com.leaf.qrcodegenerator

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.hi.dhl.binding.viewbind
import com.leaf.qrcodegenerator.databinding.ActivityMainBinding
import com.leaf.qrcodegenerator.utils.StatusBarUtil


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by viewbind()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.fitSystemBar(this)
        setContentView(binding.root)
        binding.statusBarFix.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(this)
        )
        binding.btnGenerate.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                QrCodeActivity::class.java
            )
            intent.putExtra("content", binding.etContent.text.toString().trim())
            startActivity(intent)
        }
        binding.cvCb.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                QrCodeActivity::class.java
            )
            intent.putExtra("content", binding.tvCb.text.toString().trim())
            startActivity(intent)
        }
    }

    private fun setClipboard() {
        if (getClipboardContent() != "") {
            binding.tvCb.text = getClipboardContent()
            binding.LLClipboard.visibility = View.VISIBLE
        } else
            binding.LLClipboard.visibility = View.GONE

    }

    private fun getClipboardContent(): String? {
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
        Handler().postDelayed({ setClipboard() }, 1000)
    }
}