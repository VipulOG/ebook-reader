package com.vipulog.ebookreader

import kotlinx.serialization.Serializable

@Serializable
internal data class RelocationInfo(
    val fraction: Float,
    val tocItem: TocItem? = null,
)
