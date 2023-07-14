package com.vipulog.ebookreader

import kotlinx.serialization.Serializable

@Serializable
data class SelectionInfo(
    val selectedText: String,
    val startOffset: Int,
    val endOffset: Int,
    val startNodeData: String,
    val startNodeHTML: String,
    val startNodeTagName: String,
    val endNodeData: String,
    val endNodeHTML: String,
    val endNodeTagName: String,
    val status: Int
)
