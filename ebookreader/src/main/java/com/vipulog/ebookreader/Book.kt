package com.vipulog.ebookreader

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class Book(
    val title: String? = null,
    val subtitle: String? = null,
    @Serializable(with = NameSerializer::class)
    val author: List<String>?,
    val description: String? = null,
    val cover: String? = null,
    val identifier: String? = null,
    val language: List<String>? = null,
    val publisher: String? = null,
    @Serializable(with = NameSerializer::class)
    val contributor: List<String>? = null,
    val published: String? = null,
    val modified: String? = null,
    @Serializable(with = NameSerializer::class)
    val subject: List<String>? = null,
    val rights: String? = null,
    val toc: List<TocItem>,
    val tocFraction: List<Float>,
    val theme: ReaderTheme,
)


internal object NameSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NameSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>) {
        val jsonArray = JsonArray(value.map { fullName ->
            buildJsonObject { put("name", fullName) }
        })
        encoder.encodeSerializableValue(JsonArray.serializer(), jsonArray)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonArray = decoder.decodeSerializableValue(JsonArray.serializer())
        return jsonArray.map { jsonObject ->
            jsonObject.jsonObject["name"]?.jsonPrimitive?.content ?: ""
        }
    }
}