package com.vipulog.ebookreader

import kotlinx.serialization.Serializable

@Serializable
internal data class RelocationInfo(
    val cfi: String,
    val fraction: Double,
    val tocItem: TocItem? = null,
)
