package com.vipulog.ebookreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.lifecycleScope
import com.vipulog.ebookreader.databinding.ActivityReaderBinding
import kotlinx.coroutines.launch


private val oceanicBreezeTheme = ReaderTheme(
    name = "oceanicBreeze",
    lightBg = Color.parseColor("#e6f9ff"),
    lightFg = Color.parseColor("#2c3e50"),
    darkBg = Color.parseColor("#34495e"),
    darkFg = Color.parseColor("#ecf0f1"),
    lightLink = Color.parseColor("#3498db"),
    darkLink = Color.parseColor("#5dade2"),
)


private val enchantedForestTheme = ReaderTheme(
    name = "enchantedForest",
    lightBg = Color.parseColor("#f5f5f5"),
    lightFg = Color.parseColor("#303030"),
    darkBg = Color.parseColor("#1a1a1a"),
    darkFg = Color.parseColor("#f5f5f5"),
    lightLink = Color.parseColor("#8fbc8f"),
    darkLink = Color.parseColor("#7cfc00"),
)


private val sepiaTheme = ReaderTheme(
    name = "sepia",
    lightBg = Color.parseColor("#f1e8d0"),
    lightFg = Color.parseColor("#5b4636"),
    darkBg = Color.parseColor("#342e25"),
    darkFg = Color.parseColor("#ffd595"),
    lightLink = Color.parseColor("#008b8b"),
    darkLink = Color.parseColor("#48d1cc"),
)


class ReaderActivity : AppCompatActivity(), EbookReaderEventListener, ActionMode.Callback {
    private lateinit var binding: ActivityReaderBinding
    private var actionMode: ActionMode? = null
    private val scope = lifecycleScope

    var currentTocItem: TocItem? = null
    lateinit var toc: List<TocItem>

    lateinit var currentTheme: ReaderTheme
    val themes = UniqueList<ReaderTheme>().apply {
        add(oceanicBreezeTheme)
        add(enchantedForestTheme)
        add(sepiaTheme)
    }

    var flow = ReaderFlow.PAGINATED
    var hyphenate = true
    var justify = true
    var useDark = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }


    private fun setupUI() {
        binding.appBar.menu.setGroupVisible(R.id.bookOptions, false)

        binding.ebookReader.setEbookReaderListener(this)
        binding.nextChapter.setOnClickListener { binding.ebookReader.next() }
        binding.prevChapter.setOnClickListener { binding.ebookReader.prev() }

        setupOptions()
    }


    private fun setupOptions() {
        scope.launch { binding.ebookReader.openBook(intent.data!!) }
        binding.appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.toc -> {
                    TocBottomSheet.newInstance().show(supportFragmentManager, TocBottomSheet.TAG)
                    true
                }

                R.id.readAloud -> {
                    showToast("TODO: Read Aloud")
                    true
                }

                R.id.theme -> {
                    ThemeBottomSheet.newInstance().show(supportFragmentManager, ThemeBottomSheet.TAG)
                    true
                }

                else -> false
            }
        }
    }


    override fun onBookLoaded(bookMetaData: BookMetaData) {
        binding.loading.visibility = GONE
        binding.appBar.subtitle = bookMetaData.subtitle ?: bookMetaData.title
        binding.appBar.menu.setGroupVisible(R.id.bookOptions, true)

        binding.ebookReader.getAppearance {
            themes.add(0, it)
            currentTheme = it
        }

        toc = bookMetaData.toc
    }


    override fun onBookLoadFailed(error: ReaderError) {
        error.message?.let { showToast(it) }
        finish()
    }


    override fun onProgressChanged(progress: Double, currentTocItem: TocItem?) {
        this.currentTocItem = currentTocItem
        binding.progressBar.progress = (progress * 100).toInt()
        binding.appBar.title = currentTocItem?.label ?: ""
    }


    override fun onTextSelectionModeChange(mode: Boolean) {
        if (mode) startSupportActionMode(this) else actionMode?.finish()
    }


    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }


    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        actionMode = mode
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }


    override fun onDestroyActionMode(mode: ActionMode?) {
        // TODO: Exit selection mode in reader
    }


    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.format -> {
                showToast("TODO: Format")
                true
            }

            R.id.readAloud -> {
                showToast("TODO: Read Aloud")
                true
            }

            R.id.copy -> {
                showToast("TODO: Copy")
                true
            }

            R.id.share -> {
                showToast("TODO: Share")
                true
            }

            else -> false
        }
    }


    fun applyTheme() {
        currentTheme.useDark = useDark
        currentTheme.hyphenate = hyphenate
        currentTheme.justify = justify
        currentTheme.flow = flow
        binding.navBtnContainer.visibility = if (flow == ReaderFlow.PAGINATED) GONE else VISIBLE
        binding.ebookReader.setAppearance(currentTheme)
    }


    fun gotoTocItem(tocItem: TocItem) {
        binding.ebookReader.goto(tocItem.href)
    }


    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Text Copied", text)
        clipboard.setPrimaryClip(clip)
        showToast("Copied to clipboard")
    }


    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Share Using"))
    }


    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
    }
}
