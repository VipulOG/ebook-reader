package com.vipulog.ebookreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vipulog.ebookreader.databinding.TocBottomSheetBinding
import com.vipulog.ebookreader.databinding.TocItemBinding


class TocBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: TocBottomSheetBinding
    private lateinit var activity: ReaderActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TocBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity = requireActivity() as ReaderActivity
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        binding.list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ItemAdapter()
        }
    }


    private inner class ViewHolder(itemBinding: TocItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        val item = itemBinding.item
        val textView = itemBinding.textView
        val selectionBg = itemBinding.selectionBg

        fun bind(tocItem: TocItem) {
            selectionBg.visibility =
                if (tocItem == activity.currentTocItem) View.VISIBLE else View.GONE
            item.isEnabled = tocItem != activity.currentTocItem
            textView.text = tocItem.label?.trim()

            item.setOnClickListener {
                activity.gotoTocItem(tocItem)
                dismiss()
            }
        }
    }


    private inner class ItemAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TocItemBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tocItem = activity.toc[position]
            holder.bind(tocItem)
        }

        override fun getItemCount(): Int = activity.toc.size
    }


    companion object {
        const val TAG = "TocBottomSheet"

        fun newInstance(): TocBottomSheet = TocBottomSheet()
    }
}
