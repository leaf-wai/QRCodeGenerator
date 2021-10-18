package com.leaf.qrcodegenerator

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dylanc.viewbinding.binding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kongzue.dialog.v3.MessageDialog
import com.leaf.qrcodegenerator.databinding.ActivityMainBinding
import com.leaf.qrcodegenerator.utils.AnimateUtils
import com.leaf.qrcodegenerator.utils.SPUtils
import com.leaf.qrcodegenerator.utils.StatusBarUtil
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private var startX = 0
    private var startY = 0

    private val binding: ActivityMainBinding by binding()
    private lateinit var hisAdapter: HistoryAdapter
    private lateinit var historyFragment: HistoryFragment
    private val titles = arrayOf("生成", "历史")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.lightStatusBar(this)
        setContentView(binding.root)
        binding.deleteIv.setOnClickListener {
            MessageDialog.show(this, "提示", "确认清除历史记录？", "确定", "取消")
                .setOnOkButtonClickListener { _, _ ->
                    SPUtils.clearHistory()
                    hisAdapter.setList(SPUtils.getHistory())
                    historyFragment.controlEmptyView()
                    false
                }
        }
        binding.viewPager.run {
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            offscreenPageLimit = 2
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = 2

                override fun createFragment(position: Int): Fragment {
                    historyFragment = HistoryFragment().apply {
                        hisAdapter = historyAdapter
                    }
                    return if (position == 0) GenerateFragment() else historyFragment
                }
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        AnimateUtils.getGradientAlphaAnimation(binding.deleteIv, 1F, 0F, 100)
                            .start()
                    } else {
                        AnimateUtils.getGradientAlphaAnimation(binding.deleteIv, 0F, 1F, 100)
                            .start()
                    }
                }
            })
        }
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
        TabLayoutMediator(
            binding.fixTabLayout, binding.viewPager
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val smallAlpha = abs(verticalOffset) / appBarLayout.totalScrollRange.toFloat()
            val bigAlpha = 1 - smallAlpha
            binding.tabLayout.alpha = bigAlpha
            binding.fixTabLayout.alpha = smallAlpha
            binding.tabLayout.visibility = if (abs(verticalOffset) >= appBarLayout.totalScrollRange) View.INVISIBLE else View.VISIBLE
        })
        binding.viewPagerContainer.disallowParentInterceptDownEvent(true)
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