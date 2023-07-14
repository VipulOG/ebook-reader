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
    private lateinit var listener: TocItemClickListener
    private lateinit var toc: List<TocItem>
    private var currentTocItem: TocItem? = null


    interface TocItemClickListener {
        fun onItemClick(tocItem: TocItem)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TocBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ItemAdapter()
        }
    }


    private inner class ViewHolder(binding: TocItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tocItem = binding.tocItem
        val textView = binding.textView
        val selectionBg = binding.selectionBg
    }


    private inner class ItemAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TocItemBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tocItem = toc[position]

            with(holder) {
                selectionBg.visibility = if (tocItem == currentTocItem) View.VISIBLE else View.GONE
                this.tocItem.isEnabled = tocItem != currentTocItem
                textView.text = tocItem.label?.trim()

                this.tocItem.setOnClickListener {
                    listener.onItemClick(tocItem)
                    dismiss()
                }
            }
        }

        override fun getItemCount(): Int = toc.size
    }


    companion object {
        const val TAG = "TocBottomSheet"

        fun newInstance(
            toc: List<TocItem>,
            currentTocItem: TocItem?,
            listener: TocItemClickListener
        ): TocBottomSheet {
            return TocBottomSheet().apply {
                this.toc = toc
                this.currentTocItem = currentTocItem
                this.listener = listener
            }
        }
    }
}
