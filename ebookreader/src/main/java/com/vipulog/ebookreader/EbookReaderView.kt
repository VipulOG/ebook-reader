package com.vipulog.ebookreader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream


@Suppress("MemberVisibilityCanBePrivate", "unused")
class EbookReaderView : WebView {
    private val fileServer: FileServer = FileServer()
    private var listener: EbookReaderEventListener? = null
    private val scope = CoroutineScope(Main)


    constructor(context: Context) : super(context) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    private fun init() {
        setupWebViewSettings()
        addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        fileServer.stop()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {
        val settings = this.settings
        settings.javaScriptEnabled = true
        setBackgroundColor(Color.TRANSPARENT)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()

        webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
    }


    fun setEbookReaderListener(listener: EbookReaderEventListener?) {
        this.listener = listener
    }


    suspend fun openBook(uri: Uri) {
        val isOffline = uri.scheme == "content"
        var url = uri.toString()

        if (isOffline) withContext(IO) {
            val fileName = "book.epub"
            val outputFile = File("${context.cacheDir}/ebookreader", fileName)
            url = outputFile.toURI().toString()

            if (!outputFile.exists()) outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
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

        val readerUrl = "https://appassets.androidplatform.net/assets/ebook-reader/reader.html"
        val bookUrl = if (isOffline) "http://localhost:8080/?url=$url" else url
        val uriBuilder = Uri.parse(readerUrl).buildUpon().appendQueryParameter("url", bookUrl)
        val bookReaderUrl = uriBuilder.build().toString()
        loadUrl(bookReaderUrl)
    }


    fun goto(locator: String) {
        processJavascript("goto('$locator')")
    }


    fun gotoFraction(fraction: Double) {
        processJavascript("gotoFraction($fraction)")
    }


    fun next() {
        processJavascript("next()")
    }


    fun prev() {
        processJavascript("prev()")
    }


    fun getAppearance(callback: (ReaderTheme) -> Unit) {
        processJavascript("getAppearance()") {
            callback(Json.decodeFromString(it))
        }
    }


    fun setAppearance(theme: ReaderTheme) {
        val json = Json { encodeDefaults = true }
        val themeJson = json.encodeToString(theme)
        processJavascript("setAppearance($themeJson)")
    }


    private fun processJavascript(script: String, callback: ((String) -> Unit)? = null) {
        evaluateJavascript(script) {
            val pattern = "^\"(.*)\"$".toRegex()
            val matchResult = pattern.find(it)
            val out = matchResult?.groupValues?.get(1) ?: it
            callback?.invoke(out)
        }
    }


    private inner class JavaScriptInterface {
        private val json = Json { ignoreUnknownKeys = true }

        @JavascriptInterface
        fun onBookLoaded(bookJson: String) {
            scope.launch {
                listener?.onBookLoaded(json.decodeFromString(bookJson))
            }
        }

        @JavascriptInterface
        fun onBookLoadFailed(error: String) {
            scope.launch {
                listener?.onBookLoadFailed(error)
            }
        }

        @JavascriptInterface
        fun onRelocated(relocationInfoJson: String) {
            scope.launch {
                val relocationInfo: RelocationInfo = json.decodeFromString(relocationInfoJson)
                val currentTocItem = relocationInfo.tocItem
                val fraction = relocationInfo.fraction
                listener?.onProgressChanged(fraction, currentTocItem)
            }
        }

        @JavascriptInterface
        fun onSelectionStart() {
            scope.launch {
                listener?.onTextSelectionModeChange(true)
            }
        }

        @JavascriptInterface
        fun onSelectionEnd() {
            scope.launch {
                listener?.onTextSelectionModeChange(false)
            }
        }
    }
}
