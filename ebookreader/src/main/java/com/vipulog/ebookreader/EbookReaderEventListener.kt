package com.vipulog.ebookreader

interface EbookReaderEventListener {
    fun onBookLoaded(book: Book)
    fun onBookLoadFailed(error: String)
    fun onProgressChanged(progress: Int, currentTocItem: TocItem?)
    fun onTextSelectionModeChange(mode: Boolean)
}