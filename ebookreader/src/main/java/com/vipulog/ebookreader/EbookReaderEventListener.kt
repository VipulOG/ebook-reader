package com.vipulog.ebookreader

interface EbookReaderEventListener {
    fun onBookLoaded(bookMetaData: BookMetaData)
    fun onBookLoadFailed(error: String)
    fun onProgressChanged(progress: Float, currentTocItem: TocItem?)
    fun onTextSelectionModeChange(mode: Boolean)
}