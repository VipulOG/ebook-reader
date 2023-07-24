package com.vipulog.ebookreader

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vipulog.ebookreader.databinding.ThemeBottomSheetBinding
import com.vipulog.ebookreader.databinding.ThemeItemBinding

class ThemeBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: ThemeBottomSheetBinding
    private lateinit var activity: ReaderActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ThemeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity = requireActivity() as ReaderActivity
        setupThemeSettings()
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        binding.themes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ItemAdapter()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setupThemeSettings() {
        with(binding) {
            justify.apply {
                isChecked = activity.justify
                setOnCheckedChangeListener { _, isChecked -> activity.justify = isChecked }
                activity.applyTheme()
            }

            hyphenate.apply {
                isChecked = activity.hyphenate
                setOnCheckedChangeListener { _, isChecked ->
                    activity.hyphenate = isChecked
                    activity.applyTheme()
                }
            }

            paginate.apply {
                isChecked = activity.currentTheme.flow == ReaderFlow.PAGINATED
                setOnCheckedChangeListener { _, isChecked ->
                    activity.flow = if (isChecked) ReaderFlow.PAGINATED else ReaderFlow.SCROLLED
                    activity.applyTheme()
                }
            }

            useDark.apply {
                isChecked = activity.useDark
                setOnCheckedChangeListener { _, isChecked ->
                    activity.useDark = isChecked
                    activity.applyTheme()
                    binding.themes.adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    private inner class ViewHolder(private val itemBinding: ThemeItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(themeItem: ReaderTheme) {
            val isSelected = themeItem == activity.currentTheme
            val isDark = activity.useDark
            val bg = if (isDark) themeItem.darkBg else themeItem.lightBg
            val fg = if (isDark) themeItem.darkFg else themeItem.lightFg

            with(itemBinding) {
                circleView.apply {
                    borderColor = fg

                    shadowRadius = if (isSelected) 4f else 2f
                    shadowColor = if (isSelected) fg else bg

                    circleColor = when (bg) {
                        0 -> if (fg == Color.WHITE) Color.BLACK else Color.WHITE
                        else -> bg
                    }

                    setOnClickListener {
                        activity.currentTheme = themeItem
                        activity.applyTheme()
                        binding.themes.adapter?.notifyDataSetChanged()
                    }
                }

                selectionMark.apply {
                    visibility = if (isSelected) View.VISIBLE else View.GONE
                    setColorFilter(fg)
                }
            }
        }
    }


    private inner class ItemAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ThemeItemBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val themeItem = activity.themes[position]
            holder.bind(themeItem)
        }

        override fun getItemCount(): Int = activity.themes.size
    }


    companion object {
        const val TAG = "ThemeBottomSheet"

        fun newInstance(): ThemeBottomSheet = ThemeBottomSheet()
    }
}
