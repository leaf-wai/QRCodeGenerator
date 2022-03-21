package com.leaf.qrcodegenerator

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dylanc.viewbinding.binding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kongzue.dialogx.dialogs.MessageDialog
import com.leaf.qrcodegenerator.databinding.ActivityMainBinding
import com.leaf.qrcodegenerator.utils.SPUtils
import com.leaf.qrcodegenerator.utils.StatusBarUtil
import com.permissionx.guolindev.PermissionX
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
        binding.optionContainer.setOnClickListener {
            if (binding.viewPager.currentItem == 0) {
                //扫码
                scanCode()
            } else {
                MessageDialog.show("提示", "确认清除历史记录？", "确定", "取消")
                    .setOkButton { _, _ ->
                        SPUtils.clearHistory()
                        hisAdapter.setList(SPUtils.getHistory())
                        historyFragment.controlEmptyView()
                        false
                    }
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
                    binding.optionIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            if (position == 0) R.drawable.ic_scan else R.drawable.ic_delete
                        )
                    )
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
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
            binding.tabLayout.visibility =
                if (abs(verticalOffset) >= appBarLayout.totalScrollRange) View.INVISIBLE else View.VISIBLE
        })
        binding.viewPagerContainer.disallowParentInterceptDownEvent(true)
    }

    private fun scanCode() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList, _ ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "${getString(R.string.app_name)} 需要您同意以下权限才能保存二维码",
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
                    startActivity(Intent(this, ScanActivity::class.java))
                } else {
                    Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT)
                        .show()
                }
            }
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