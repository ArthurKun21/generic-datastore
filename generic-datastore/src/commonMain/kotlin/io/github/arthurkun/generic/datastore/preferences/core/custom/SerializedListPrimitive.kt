package io.github.arthurkun.generic.datastore.preferences.core.custom

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
 * [CustomGenericPreferenceItem] that stores a [List] inside one JSON array string.
 *
 * The outer JSON parse falls back to [defaultValue]. Individual elements that fail to deserialize
 * are skipped.
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
