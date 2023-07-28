package com.vipulog.ebookreader

interface EbookReaderEventListener {
    fun onBookLoaded(bookMetaData: BookMetaData)
    fun onBookLoadFailed(error: ReaderError)
    fun onProgressChanged(progress: Double, currentTocItem: TocItem?)
    fun onTextSelectionModeChange(mode: Boolean)
}