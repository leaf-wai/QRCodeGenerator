package com.leaf.qrcodegenerator

import android.content.Intent
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dylanc.viewbinding.brvah.getViewBinding
import com.dylanc.viewbinding.brvah.withBinding
import com.leaf.qrcodegenerator.databinding.ItemHistoryBinding
import com.leaf.qrcodegenerator.utils.ClipboardUtils

class HistoryAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_history) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return super.onCreateDefViewHolder(parent, viewType).withBinding { ItemHistoryBinding.bind(it) }
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.getViewBinding<ItemHistoryBinding>().apply {
            tvHistory.text = item
            cvHistory.setOnClickListener {
                context.startActivity(Intent(context, QrCodeActivity::class.java).apply {
                    putExtra("content", item)
                })
            }
            cvHistory.setOnLongClickListener{
                ClipboardUtils.copyToClipboard(context, item)
                true
            }
        }
    }


}