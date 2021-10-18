package com.leaf.qrcodegenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dylanc.viewbinding.binding
import com.leaf.qrcodegenerator.databinding.FragmentGenerateBinding
import com.leaf.qrcodegenerator.utils.ClipboardUtils
import com.leaf.qrcodegenerator.utils.SPUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GenerateFragment : Fragment(R.layout.fragment_generate) {

    private val binding: FragmentGenerateBinding by binding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGenerate.setOnClickListener {
            if (binding.etContent.text?.trim()?.isEmpty() == true) {
                Toast.makeText(requireContext(), "内容不能为空", Toast.LENGTH_SHORT)
                    .show()
            } else {
                SPUtils.saveHistory(binding.etContent.text.toString().trim())
                startActivity(Intent(requireActivity(), QrCodeActivity::class.java).apply {
                    putExtra("content", binding.etContent.text.toString().trim())
                })
            }
        }
        binding.cvCb.setOnClickListener {
            val content = binding.tvCb.text.toString().trim()
            SPUtils.saveHistory(content)
            startActivity(Intent(requireActivity(), QrCodeActivity::class.java).apply {
                putExtra("content", content)
            })
        }
    }

    private fun setClipboard() {
        if (ClipboardUtils.getClipboardContent(requireContext()).trim().isNotEmpty()) {
            binding.tvCb.text = ClipboardUtils.getClipboardContent(requireContext())
            binding.LLClipboard.visibility = View.VISIBLE
        } else
            binding.LLClipboard.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            setClipboard()
        }
    }
}