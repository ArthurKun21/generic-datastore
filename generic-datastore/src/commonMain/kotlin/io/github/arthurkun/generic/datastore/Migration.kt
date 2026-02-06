@file:Suppress("unused")

package io.github.arthurkun.generic.datastore

import kotlinx.serialization.SerializationException
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

private fun Any?.toJsonElementImpl(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Collection<*> -> JsonArray(map { it.toJsonElementImpl() })
    is Map<*, *> -> JsonObject(map { it.key.toString() to it.value.toJsonElementImpl() }.toMap())
    else -> JsonPrimitive(this.toString())
}

@Deprecated(
    message = "Use PreferenceBackupCreator for type-safe backup/restore",
    replaceWith = ReplaceWith(
        "PreferenceBackupCreator(datastore).createBackup()",
        "io.github.arthurkun.generic.datastore.backup.PreferenceBackupCreator",
    ),
    level = DeprecationLevel.HIDDEN,
)
fun Any?.toJsonElement(): JsonElement = toJsonElementImpl()

private fun parseJsonValue(element: JsonElement): Any? {
    return when (element) {
        is JsonNull -> null
        is JsonPrimitive -> when {
            element.isString -> element.content
            element.booleanOrNull != null -> element.boolean
            element.longOrNull != null -> element.long
            element.doubleOrNull != null -> element.double
            else -> element.toString()
        }
        is JsonArray -> element.map { parseJsonValue(it) }
        is JsonObject -> element.mapValues { parseJsonValue(it.value) }
    }
}

@Deprecated(
    message = "Use PreferenceBackupRestorer for type-safe backup/restore",
    replaceWith = ReplaceWith(
        "PreferenceBackupRestorer(datastore).restoreFromJson(jsonString)",
        "io.github.arthurkun.generic.datastore.backup.PreferenceBackupRestorer",
    ),
    level = DeprecationLevel.HIDDEN,
)
fun String.toJsonMap(): Map<String, Any?> = try {
    Json.parseToJsonElement(this)
        .jsonObject
        .mapValues { (_, v) -> parseJsonValue(v) }
} catch (e: SerializationException) {
    throw IllegalArgumentException("Failed to parse JSON string in toJsonMap: ${e.message}", e)
} catch (e: IllegalArgumentException) {
    throw IllegalArgumentException("Failed to parse JSON string in toJsonMap: ${e.message}", e)
}
