package com.leaf.qrcodegenerator

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dylanc.viewbinding.binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.leaf.qrcodegenerator.databinding.ActivityMainBinding
import com.leaf.qrcodegenerator.utils.SPUtils
import com.leaf.qrcodegenerator.utils.StatusBarUtil
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private var startX = 0
    private var startY = 0

    private val binding: ActivityMainBinding by binding()
    private lateinit var hisAdapter: HistoryAdapter
    private val titles = arrayOf("生成", "历史")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.lightStatusBar(this)
        setContentView(binding.root)
        binding.deleteIv.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("确认清除历史记录？").setPositiveButton(
                "确定"
            ) { _, _ ->
                SPUtils.clearHistory()
                hisAdapter.setList(SPUtils.getHistory())
            }
                .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }.show()
        }
        binding.viewPager.run {
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            offscreenPageLimit = 2
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = 2

                override fun createFragment(position: Int): Fragment {
                    return if (position == 0) GenerateFragment() else HistoryFragment().apply {
                        hisAdapter = historyAdapter
                    }
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.deleteIv.visibility = if (position == 1) View.VISIBLE else View.GONE
                }
            })
        }
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x.toInt()
                val endY = ev.y.toInt()
                val disX = abs(endX - startX)
                val disY = abs(endY - startY)
                if (disX < disY) {
                    binding.viewPager.isUserInputEnabled = false
                }
            }
            MotionEvent.ACTION_UP -> {
                startX = 0
                startY = 0
                binding.viewPager.isUserInputEnabled = true
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}