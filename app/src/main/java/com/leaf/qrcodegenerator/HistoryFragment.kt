package com.leaf.qrcodegenerator

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.dylanc.viewbinding.binding
import com.leaf.qrcodegenerator.databinding.FragmentHistoryBinding
import com.leaf.qrcodegenerator.utils.SPUtils


class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val binding: FragmentHistoryBinding by binding()
    val historyAdapter = HistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter.run {
            setList(SPUtils.getHistory())
            isAnimationFirstOnly = false
            setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        }
        binding.historyRv.run {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    fun controlEmptyView() {
        binding.emptyLayout.root.visibility =
            if (SPUtils.getHistory().isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (SPUtils.getHistory() != historyAdapter.data) {
            historyAdapter.setList(SPUtils.getHistory())
        }
        controlEmptyView()
    }
}