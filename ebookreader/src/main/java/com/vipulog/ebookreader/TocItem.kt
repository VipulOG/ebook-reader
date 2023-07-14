package com.vipulog.ebookreader

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
data class TocItem(
    @Serializable(with = IdSerializer::class)
    val id: String,
    val href: String,
    val label: String? = null,
    val subitems: List<TocItem>? = null,
    val parent: String? = null,
)

object IdSerializer : JsonTransformingSerializer<String>(String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonPrimitive -> JsonPrimitive(element.content)
            else -> error("Expected a string value for 'id' field")
        }
    }
}