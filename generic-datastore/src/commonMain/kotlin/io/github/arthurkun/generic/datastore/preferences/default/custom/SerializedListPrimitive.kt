package io.github.arthurkun.generic.datastore.preferences.default.custom

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

/**
 * A [CustomGenericPreferenceItem] for storing a [List] of custom objects using
 * per-element serialization functions.
 *
 * Each element is serialized to a string via [elementSerializer] and wrapped in a
 * [JsonArray] for storage. On retrieval, each element in the JSON array is deserialized
 * via [elementDeserializer]. Individual elements that fail deserialization are skipped
 * rather than causing the entire list to fall back to [defaultValue].
 *
 * @param T The type of each element in the list.
 * @param datastore The [DataStore] instance used for storing preferences.
 * @param key The unique string key used to identify this preference within the DataStore.
 * @param defaultValue The default list returned when the preference is not set or
 *   the entire JSON string cannot be parsed.
 * @param elementSerializer A function to serialize an element of type [T] to its
 *   [String] representation.
 * @param elementDeserializer A function to deserialize a [String] back to an element
 *   of type [T].
 * @param ioDispatcher The [CoroutineDispatcher] to use for I/O operations.
 *   Defaults to [Dispatchers.IO].
 */
internal class SerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    defaultValue: List<T>,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
    defaultValue = defaultValue,
    serializer = { list -> JsonArray(list.map { JsonPrimitive(elementSerializer(it)) }).toString() },
    deserializer = { str ->
        Json.parseToJsonElement(str).jsonArray.mapNotNull { element ->
            try {
                elementDeserializer(element.jsonPrimitive.content)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    },
    ioDispatcher = ioDispatcher,
)
