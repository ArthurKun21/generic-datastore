package io.github.arthurkun.generic.datastore.preferences.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

/**
 * Deserializes [value] with [deserializer], rethrowing [CancellationException] and mapping every
 * other failure to [fallback].
 */
internal fun <T> deserializeOrDefault(
    value: String,
    fallback: T,
    deserializer: (String) -> T,
): T = try {
    deserializer(value)
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    fallback
}

/**
 * Deserializes [value] with [deserializer], rethrowing [CancellationException] and mapping every
 * other failure to `null`.
 */
internal fun <T> deserializeOrNull(value: String, deserializer: (String) -> T): T? = try {
    deserializer(value)
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    null
}

/**
 * Deserializes each element of a stored string set with [deserializer], rethrowing
 * [CancellationException] and skipping only elements that fail to deserialize.
 */
internal fun <T> deserializeSet(values: Set<String>, deserializer: (String) -> T): Set<T> {
    val elements = mutableSetOf<T>()
    values.forEach { value ->
        try {
            elements.add(deserializer(value))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Skip only elements that failed to deserialize.
        }
    }
    return elements
}

/**
 * Encodes [list] as a JSON array string where every element is individually converted to a
 * [String] with [elementSerializer] and wrapped as a JSON string literal.
 */
internal fun <T> serializeList(list: List<T>, elementSerializer: (T) -> String): String =
    JsonArray(list.map { JsonPrimitive(elementSerializer(it)) }).toString()

/**
 * Decodes a JSON array string produced by [serializeList], rethrowing [CancellationException].
 *
 * The outer value must be a valid JSON array of JSON strings; a malformed outer payload throws
 * (callers apply their own fallback — [deserializeOrDefault] maps it to the default value,
 * [deserializeOrNull] maps it to `null`). Elements that are not JSON strings or that fail to
 * deserialize with [elementDeserializer] are skipped. [CancellationException] from either the
 * outer parse or an element is always rethrown.
 */
internal fun <T> deserializeList(value: String, elementDeserializer: (String) -> T): List<T> {
    // Parse the outer array outside the per-element try so a malformed payload propagates to the
    // caller's fallback instead of being mistaken for "zero valid elements".
    val array =
        try {
            Json.parseToJsonElement(value).jsonArray
        } catch (e: CancellationException) {
            throw e
        }
    val elements = mutableListOf<T>()
    array.forEach { element ->
        try {
            elements.add(elementDeserializer(element.jsonPrimitive.content))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Skip only elements that failed to deserialize.
        }
    }
    return elements
}
