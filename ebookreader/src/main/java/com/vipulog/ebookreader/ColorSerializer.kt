package com.vipulog.ebookreader

import android.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


internal object ColorSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeString(convertColorIntToString(value))
    }

    override fun deserialize(decoder: Decoder): Int {
        val colorString = decoder.decodeString()
        return parseColorFromString(colorString)
    }

    private fun convertColorIntToString(colorInt: Int): String {
        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)
        val alpha = Color.alpha(colorInt)

        return if (alpha < 255) {
            "RGBA($red, $green, $blue, ${alpha / 255.0})"
        } else {
            "RGB($red, $green, $blue)"
        }
    }

    private fun parseColorFromString(colorString: String): Int {
        val color = colorString.lowercase()

        if (color.startsWith("rgba")) {
            val values = color.substringAfter("(").substringBefore(")").split(",")
            val red = values[0].trim().toInt()
            val green = values[1].trim().toInt()
            val blue = values[2].trim().toInt()
            val alpha = (values[3].trim().toFloat() * 255).toInt()
            return Color.argb(alpha, red, green, blue)
        } else if (color.startsWith("rgb")) {
            val values = color.substringAfter("(").substringBefore(")").split(",")
            val red = values[0].trim().toInt()
            val green = values[1].trim().toInt()
            val blue = values[2].trim().toInt()
            return Color.rgb(red, green, blue)
        } else if (color.startsWith("#")) {
            return Color.parseColor(color)
        } else if (color.startsWith("0x")) {
            val hexString = color.substring(2)
            if (hexString.length == 6) {
                return Color.parseColor("#$hexString")
            } else if (hexString.length == 8) {
                val alpha = hexString.substring(0, 2).toInt(16)
                val rgb = hexString.substring(2)
                return Color.parseColor("#${alpha.toString(16)}$rgb")
            }
        }

        throw IllegalArgumentException("Invalid color format")
    }
}
