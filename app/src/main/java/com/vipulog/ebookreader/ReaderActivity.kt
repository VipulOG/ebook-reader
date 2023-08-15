package com.vipulog.ebookreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.lifecycleScope
import com.vipulog.ebookreader.databinding.ActivityReaderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ReaderActivity : AppCompatActivity(), EbookReaderEventListener, ActionMode.Callback {
    private lateinit var binding: ActivityReaderBinding
    private var actionMode: ActionMode? = null
    private val scope = lifecycleScope

    private lateinit var sanitizedBookId: String
    var currentTocItem: TocItem? = null
    lateinit var toc: List<TocItem>

    lateinit var currentTheme: ReaderTheme
    val themes = arrayListOf<ReaderTheme>()

    var flow = ReaderFlow.PAGINATED
    var hyphenate = true
    var justify = true
    var useDark = false


    init {
        val oceanicBreezeTheme = ReaderTheme(
            name = "oceanicBreeze",
            lightBg = Color.parseColor("#e6f9ff"),
            lightFg = Color.parseColor("#2c3e50"),
            darkBg = Color.parseColor("#34495e"),
            darkFg = Color.parseColor("#ecf0f1"),
            lightLink = Color.parseColor("#3498db"),
            darkLink = Color.parseColor("#5dade2"),
        )


        val enchantedForestTheme = ReaderTheme(
            name = "enchantedForest",
            lightBg = Color.parseColor("#f5f5f5"),
            lightFg = Color.parseColor("#303030"),
            darkBg = Color.parseColor("#1a1a1a"),
            darkFg = Color.parseColor("#f5f5f5"),
            lightLink = Color.parseColor("#8fbc8f"),
            darkLink = Color.parseColor("#7cfc00"),
        )


        val sepiaTheme = ReaderTheme(
            name = "sepia",
            lightBg = Color.parseColor("#f1e8d0"),
            lightFg = Color.parseColor("#5b4636"),
            darkBg = Color.parseColor("#342e25"),
            darkFg = Color.parseColor("#ffd595"),
            lightLink = Color.parseColor("#008b8b"),
            darkLink = Color.parseColor("#48d1cc"),
        )

        themes.apply {
            add(oceanicBreezeTheme)
            add(enchantedForestTheme)
            add(sepiaTheme)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupBackPressedHandler()

        scope.launch { binding.ebookReader.openBook(intent.data!!) }
    }


    private fun setupUI() {
        binding.appBar.setNavigationOnClickListener {
            finish()
        }

        binding.ebookReader.setEbookReaderListener(this)
        binding.nextChapter.setOnClickListener { binding.ebookReader.next() }
        binding.prevChapter.setOnClickListener { binding.ebookReader.prev() }

        binding.appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.toc -> {
                    TocBottomSheet.newInstance().show(supportFragmentManager, TocBottomSheet.TAG)
                    true
                }

                R.id.theme -> {
                    ThemeBottomSheet.newInstance()
                        .show(supportFragmentManager, ThemeBottomSheet.TAG)
                    true
                }

                else -> false
            }
        }
    }


    private fun setupBackPressedHandler() {
        var lastBackPressedTime: Long = 0
        val doublePressInterval: Long = 2000

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.ebookReader.canGoBack()) {
                    binding.ebookReader.goBack()
                } else {
                    if (lastBackPressedTime + doublePressInterval > System.currentTimeMillis()) {
                        finish()
                    } else {
                        Toast.makeText(
                            this@ReaderActivity,
                            "Press back again to exit",
                            Toast.LENGTH_SHORT
                        ).show()
                        lastBackPressedTime = System.currentTimeMillis()
                    }
                }
            }
        })
    }


    override fun onBookLoaded(book: Book) {
        toc = book.toc
        val bookId = book.identifier!!

        val illegalCharsRegex = Regex("[^a-zA-Z0-9._-]")
        sanitizedBookId = bookId.replace(illegalCharsRegex, "_")

        val cfi = loadData<String>("${sanitizedBookId}_progress", baseContext)
        cfi?.let { binding.ebookReader.goto(it) }

        binding.appBar.subtitle = book.subtitle ?: book.title
        binding.appBar.menu.setGroupVisible(R.id.bookOptions, true)

        binding.ebookReader.getAppearance {
            themes.add(0, it)
            currentTheme = it
        }

        binding.loading.visibility = GONE
    }


    override fun onBookLoadFailed(error: ReaderError) {
        error.message?.let { showToast(it) }
        finish()
    }


    override fun onProgressChanged(cfi: String, progress: Double, currentTocItem: TocItem?) {
        this.currentTocItem = currentTocItem
        binding.progressBar.progress = (progress * 100).toInt()
        binding.appBar.title = currentTocItem?.label ?: ""
        saveData("${sanitizedBookId}_progress", cfi, baseContext)
    }


    override fun onTextSelectionModeChange(mode: Boolean) {
        if (mode) startSupportActionMode(this) else actionMode?.finish()
    }


    override fun onImageSelected(base64String: String) {
        scope.launch(Dispatchers.IO) {
            val base64Data = base64String.substringAfter(",")
            val imageBytes: ByteArray = Base64.decode(base64Data, Base64.DEFAULT)
            val imageFile = File(cacheDir, "image.jpg")

            if (imageFile.exists()) imageFile.delete()

            try {
                FileOutputStream(imageFile).use { outputStream -> outputStream.write(imageBytes) }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val intent = Intent(this@ReaderActivity, ImagePreviewActivity::class.java)
            intent.data = Uri.fromFile(imageFile)
            startActivity(intent)
        }
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
