package io.github.arthurkun.generic.datastore

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Collection<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElement() }.toMap())
    else -> JsonPrimitive(this.toString())
}

private fun parseJsonValue(element: JsonElement): Any {
    return when (element) {
        is JsonNull -> ""
        is JsonPrimitive -> {
            when {
                element.isString -> element.content
                element.booleanOrNull != null -> element.boolean
                element.longOrNull != null -> element.long
                element.doubleOrNull != null -> element.double
                else -> element.toString()
            }
        }

        is JsonArray -> element.map { parseJsonValue(it) }
        is JsonObject -> element.mapValues { parseJsonValue(it.value) }
    }
}

/**
 * Parse a JSON string to a Map<String, Any>
 */
fun String.toJsonMap(): Map<String, Any> = Json.parseToJsonElement(this)
    .jsonObject
    .mapNotNull { (k, v) ->
        if (v is JsonNull) null else k to parseJsonValue(v)
    }
    .toMap()
