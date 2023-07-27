package com.vipulog.ebookreader

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vipulog.ebookreader.databinding.HeaderItemBinding
import com.vipulog.ebookreader.databinding.OpenBookFromUrlBottomSheetBinding


class OpenBookFromUrlBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: OpenBookFromUrlBottomSheetBinding
    private lateinit var adapter: ItemAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OpenBookFromUrlBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ItemAdapter()
        binding.headersRv.layoutManager = LinearLayoutManager(context)
        binding.headersRv.adapter = adapter

        binding.addHeaderBtn.setOnClickListener { adapter.insert(Header()) }

        binding.openBtn.setOnClickListener {
            val uri = binding.tilUrl.editText?.text.toString().toUri()
            val method = binding.tilRequestMethod.editText?.text.toString()
            val bundle = Bundle()
            bundle.putString("method", method)
            bundle.putParcelableArrayList("headers", adapter.items)
            val intent = Intent(requireActivity(), ReaderActivity::class.java)
            intent.putExtras(bundle)
            intent.data = uri
            startActivity(intent)
        }
    }


    private inner class ViewHolder(itemBinding: HeaderItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        val tilKey = itemBinding.tilKey
        val tilVal = itemBinding.tilValue
        val deleteBtn = itemBinding.deleteBtn

        fun bind(position: Int) {
            val item = adapter.items[position]

            tilKey.editText?.setText(item.key)
            tilKey.editText?.doOnTextChanged { text, _, _, _ ->
                item.key = text.toString()
            }

            tilVal.editText?.setText(item.value)
            tilVal.editText?.doOnTextChanged { text, _, _, _ ->
                item.value = text.toString()
            }

            deleteBtn.setOnClickListener {
                binding.headersRv.focusedChild?.clearFocus()
                adapter.removeAt(adapterPosition)
            }
        }
    }


    private inner class ItemAdapter : RecyclerView.Adapter<ViewHolder>() {
        val items = ArrayList<Header>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = HeaderItemBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = items.size

        fun insert(header: Header) {
            items.add(header)
            notifyItemInserted(items.lastIndex)
        }

        fun removeAt(position: Int) {
            if (position >= 0) {
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
        }
    }


    companion object {
        const val TAG = "OpenBookFromUrlBottomSheet"

        fun newInstance(): OpenBookFromUrlBottomSheet = OpenBookFromUrlBottomSheet()
    }
}
