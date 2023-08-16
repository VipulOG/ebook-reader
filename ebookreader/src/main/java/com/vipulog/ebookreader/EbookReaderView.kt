package com.vipulog.ebookreader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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

    private val domain = "appassets.androidplatform.net"
    private val readerUrl = "https://$domain/assets/ebook-reader/reader.html"

    private val navigationStack = ArrayDeque<String>()


    constructor(context: Context) : super(context) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    private fun init() {
        setupWebViewSettings()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        fileServer.stop()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {
        settings.javaScriptEnabled = true
        setBackgroundColor(Color.TRANSPARENT)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()

        webViewClient = object : WebViewClientCompat() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url
                val sanitizedUrl = Uri.parse(url.toString().replace("blob:", ""))
                if (!sanitizedUrl.host.equals(domain, ignoreCase = true)) {
                    val intent = Intent(Intent.ACTION_VIEW, url)
                    view.context.startActivity(intent)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")

        setOnLongClickListener {
            val result = hitTestResult
            if (result.type == HitTestResult.IMAGE_TYPE) {
                val imageUrl = result.extra
                processJavascript(
                    "getBlobAsBase64('$imageUrl', (res) => { AndroidInterface.onImageSelected(res) })"
                )
                return@setOnLongClickListener true
            } else {
                return@setOnLongClickListener false
            }
        }
    }


    fun setEbookReaderListener(listener: EbookReaderEventListener?) {
        this.listener = listener
    }


    suspend fun openBook(uri: Uri) {
        val fileName = "book.epub"
        val outputFile = File("${context.cacheDir}/ebookreader", fileName)
        val url = outputFile.toURI().toString()

        if (!outputFile.exists()) outputFile.parentFile?.mkdirs()
        withContext(IO) {
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

        val bookUrl = "http://localhost:${fileServer.listeningPort}/?url=$url"
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


    override fun canGoBack(): Boolean {
        return navigationStack.size > 1
    }


    override fun goBack() {
        if (canGoBack()) {
            navigationStack.removeLast()
            val cfi = navigationStack.last()
            processJavascript("goto('$cfi')")
            navigationStack.removeLast()
        }
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
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        @JavascriptInterface
        fun onBookLoaded(bookJson: String) {
            scope.launch {
                listener?.onBookLoaded(json.decodeFromString(bookJson))
            }
        }

        @JavascriptInterface
        fun onBookLoadFailed(error: String) {
            scope.launch {
                listener?.onBookLoadFailed(Json.decodeFromString(error))
            }
        }

        @JavascriptInterface
        fun onRelocated(relocationInfoJson: String) {
            scope.launch {
                val relocationInfo: RelocationInfo = json.decodeFromString(relocationInfoJson)
                navigationStack.add(relocationInfo.cfi)
                listener?.onProgressChanged(relocationInfo)
            }
        }

        @JavascriptInterface
        fun onImageSelected(imageBase64: String) {
            scope.launch {
                listener?.onImageSelected(imageBase64)
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
