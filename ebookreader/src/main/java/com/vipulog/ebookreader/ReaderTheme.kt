package com.vipulog.ebookreader


import android.graphics.Color
import kotlinx.serialization.*

@Serializable
data class ReaderTheme(
    var lineHeight: Float = 1.4f,
    var justify: Boolean = true,
    var hyphenate: Boolean = true,
    var invert: Boolean = false,
    @SerialName("themeName")
    val name: String,
    @Serializable(with = ColorSerializer::class)
    var lightBg: Int = Color.parseColor("#ffffff"),
    @Serializable(with = ColorSerializer::class)
    var lightFg: Int = Color.parseColor("#000000"),
    @Serializable(with = ColorSerializer::class)
    var lightLink: Int = Color.parseColor("#0066cc"),
    @Serializable(with = ColorSerializer::class)
    var darkBg: Int = Color.parseColor("#222222"),
    @Serializable(with = ColorSerializer::class)
    var darkFg: Int = Color.parseColor("#e0e0e0"),
    @Serializable(with = ColorSerializer::class)
    var darkLink: Int = Color.parseColor("#88ccee"),
    var gap: Float = 0.06f,
    var maxInlineSize: Int = 720,
    var maxBlockSize: Int = 1440,
    var maxColumnCount: Int = 2,
    var flow: ReaderFlow = ReaderFlow.PAGINATED,
    var useDark: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReaderTheme) return false

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

enum class ReaderFlow {
    @SerialName("scrolled")
    SCROLLED,

    @SerialName("paginated")
    PAGINATED,
}
