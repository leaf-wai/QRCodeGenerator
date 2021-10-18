package com.leaf.qrcodegenerator

import android.annotation.SuppressLint
import android.content.Intent
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dylanc.viewbinding.brvah.getViewBinding
import com.dylanc.viewbinding.brvah.withBinding
import com.leaf.qrcodegenerator.databinding.ItemHistoryBinding
import com.leaf.qrcodegenerator.utils.ClipboardUtils
import java.util.*
import kotlin.math.sqrt

class HistoryAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_history) {

    private var isLongClickModule = false
    var startX = 0f
    var startY = 0f
    var timer:Timer? = null


    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return super.onCreateDefViewHolder(parent, viewType)
            .withBinding { ItemHistoryBinding.bind(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.getViewBinding<ItemHistoryBinding>().apply {
            tvHistory.text = item
            cvHistory.setOnClickListener {
                context.startActivity(Intent(context, QrCodeActivity::class.java).apply {
                    putExtra("content", item)
                })
            }
//            cvHistory.setOnLongClickListener {
//                ClipboardUtils.copyToClipboard(context, item)
//                true
//            }
            cvHistory.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = v.x
                        startY = v.y
                        timer = Timer()
                        timer?.schedule(object:TimerTask(){
                            override fun run() {
                                isLongClickModule = true
                            }
                        },600)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = sqrt((v.x - startX) * (v.x - startX) + (v.y - startY) * (v.y - startY))
                        if (deltaX > 20 && timer != null) {
                            timer?.cancel()
                            timer = null
                        }
                        if (isLongClickModule){
                            ClipboardUtils.copyToClipboard(context, item)
                            timer = null
                        }
                    }
                    else -> {
                        isLongClickModule = false
                        if (timer != null) {
                            timer?.cancel()
                            timer = null
                        }
                    }
                }
                false
            }
        }
    }

}