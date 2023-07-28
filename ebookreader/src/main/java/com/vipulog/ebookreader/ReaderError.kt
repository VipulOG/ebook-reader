package com.vipulog.ebookreader

import kotlinx.serialization.Serializable

@Serializable
data class ReaderError(
    val name: String? = null,
    val message: String? = null,
    val stack: String? = null,
)
