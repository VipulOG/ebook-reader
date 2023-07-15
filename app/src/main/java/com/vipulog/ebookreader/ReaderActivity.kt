package com.vipulog.ebookreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class ReaderActivity : AppCompatActivity(), EbookReaderEventListener, ActionMode.Callback {
    private lateinit var binding: ActivityReaderBinding
    private var actionMode: ActionMode? = null
    private val scope = lifecycleScope

    private lateinit var tocSheet: TocBottomSheet
    private var currentTocItem: TocItem? = null

    private val themes = UniqueList<ReaderTheme>().apply {
        add(lightTheme)
        add(darkTheme)
        add(sepiaTheme)
    }

    val lightTheme = ReaderTheme(
        name = "light",
        backgroundColor = Color.WHITE,
        textColor = Color.BLACK,
        fontSize = 16f,
        lineHeight = 20f,
        paragraphSpacing = 1.5f,
        justify = true,
        hyphenate = true
    )

    val darkTheme = ReaderTheme(
        name = "dark",
        backgroundColor = Color.BLACK,
        textColor = Color.WHITE,
        fontSize = 16f,
        lineHeight = 20f,
        paragraphSpacing = 1.5f,
        justify = true,
        hyphenate = true
    )

    val sepiaTheme = ReaderTheme(
        name = "sepia",
        backgroundColor = Color.parseColor("#F4EFE0"),
        textColor = Color.parseColor("#4D4433"),
        fontSize = 16f,
        lineHeight = 20f,
        paragraphSpacing = 1.5f,
        justify = true,
        hyphenate = true
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openBook()
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
        binding.appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.toc -> {
                    tocSheet.show(supportFragmentManager, TocBottomSheet.TAG)
                    true
                }

                R.id.readAloud -> {
                    showToast("TODO: Read Aloud")
                    true
                }

                R.id.theme -> {
                    val listener = object : ThemeBottomSheet.ThemeChangeListener {
                        override fun onThemeChange(newTheme: ReaderTheme) {
                            binding.ebookReader.setTheme(newTheme)
                        }

                        override fun onFlowChange(newFlow: String) {
                            binding.navBtnContainer.visibility =
                                if (newFlow == "paginated") GONE else VISIBLE
                            binding.ebookReader.setFlow(newFlow)
                        }
                    }
                    binding.ebookReader.getTheme { theme ->
                        binding.ebookReader.getFlow {
                            val flow = it ?: "paginated"
                            val themeSheet =
                                ThemeBottomSheet.newInstance(themes, theme, flow, listener)
                            themeSheet.show(supportFragmentManager, ThemeBottomSheet.TAG)
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }


    override fun onBookLoaded(book: Book) {
        binding.loading.visibility = GONE
        binding.appBar.subtitle = book.subtitle ?: book.title
        binding.appBar.menu.setGroupVisible(R.id.bookOptions, true)

        binding.ebookReader.getTheme {
            themes.add(0, it)
        }

        tocSheet = TocBottomSheet.newInstance(book.toc, currentTocItem, object :
            TocBottomSheet.TocItemClickListener {
            override fun onItemClick(tocItem: TocItem) {
                binding.ebookReader.goto(tocItem.href)
                tocSheet.dismiss()
            }
        })
    }


    override fun onBookLoadFailed(error: String) {
        showToast("Error: $error")
        finish()
    }


    override fun onProgressChanged(progress: Int, currentTocItem: TocItem?) {
        this.currentTocItem = currentTocItem
        binding.progressBar.progress = progress
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


    private fun openBook() {
        scope.launch {
            val fileUri = requireNotNull(intent.data)
            val fileName = "book.epub"
            val outputFile = File("$cacheDir/epubreader", fileName)
            copy(outputFile, fileUri)
            binding.ebookReader.openBook(outputFile.toURI().toString())
        }
    }


    private suspend fun copy(outputFile: File, fileUri: Uri) {
        withContext(Dispatchers.IO) {
            if (!outputFile.exists()) outputFile.parentFile?.mkdirs()

            contentResolver.openInputStream(fileUri)?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
        }
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
