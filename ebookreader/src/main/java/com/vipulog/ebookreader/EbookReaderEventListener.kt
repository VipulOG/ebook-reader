package com.vipulog.ebookreader

interface EbookReaderEventListener {
    fun onBookLoaded(book: Book)
    fun onBookLoadFailed(error: ReaderError)
    fun onProgressChanged(info: RelocationInfo)
    fun onTextSelectionModeChange(mode: Boolean)
    fun onImageSelected(base64String: String)
}