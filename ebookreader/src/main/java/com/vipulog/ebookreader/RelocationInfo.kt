package com.vipulog.ebookreader

import kotlinx.serialization.Serializable

@Serializable
data class RelocationInfo(
    val cfi: String,
    val fraction: Double,
    val currentLocation: Int,
    val nextLocation: Int,
    val totalLocations: Int,
    val tocItem: TocItem? = null,
)
