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
import kotlin.math.roundToInt

class ThemeBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: ThemeBottomSheetBinding
    private lateinit var listener: ThemeChangeListener
    private lateinit var themes: List<ReaderTheme>
    private lateinit var currentTheme: ReaderTheme


    interface ThemeChangeListener {
        fun onThemeChange(newTheme: ReaderTheme)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ThemeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupThemeSettings()
    }


    private fun setupRecyclerView() {
        binding.themes.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ItemAdapter()
        }
    }


    private fun setupThemeSettings() {
        with(binding) {
            justify.apply {
                isChecked = currentTheme.justify
                setOnCheckedChangeListener { _, isChecked ->
                    currentTheme.justify = isChecked
                    listener.onThemeChange(currentTheme)
                }
            }

            hyphenate.apply {
                isChecked = currentTheme.hyphenate
                setOnCheckedChangeListener { _, isChecked ->
                    currentTheme.hyphenate = isChecked
                    listener.onThemeChange(currentTheme)
                }
            }

            paginate.apply {
                isChecked = currentTheme.flow == ReaderFlow.PAGINATED
                setOnCheckedChangeListener { _, isChecked ->
                    currentTheme.flow = if (isChecked) ReaderFlow.PAGINATED else ReaderFlow.SCROLLED
                    listener.onThemeChange(currentTheme)
                }
            }
        }
    }


    private inner class ViewHolder(private val itemBinding: ThemeItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(themeItem: ReaderTheme) {
            val isSelected = themeItem == currentTheme
            val isDark = themeItem.isDark
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
                        currentTheme = themeItem
                        listener.onThemeChange(currentTheme)
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
            val themeItem = themes[position]
            holder.bind(themeItem)
        }

        override fun getItemCount(): Int = themes.size
    }


    companion object {
        const val TAG = "ThemeBottomSheet"

        fun roundToStep(value: Float, rangeStart: Float, rangeEnd: Float, stepSize: Float): Float {
            val roundedValue = (value / stepSize).roundToInt() * stepSize
            return when {
                roundedValue < rangeStart -> rangeStart
                roundedValue > rangeEnd -> rangeEnd
                else -> roundedValue
            }
        }

        fun newInstance(
            themes: List<ReaderTheme>,
            currentTheme: ReaderTheme,
            listener: ThemeChangeListener
        ): ThemeBottomSheet {
            return ThemeBottomSheet().apply {
                this.themes = themes
                this.currentTheme = currentTheme
                this.listener = listener
            }
        }
    }
}
