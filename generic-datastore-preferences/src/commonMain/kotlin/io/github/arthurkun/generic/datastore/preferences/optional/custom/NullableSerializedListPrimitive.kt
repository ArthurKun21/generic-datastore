package io.github.arthurkun.generic.datastore.preferences.optional.custom

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
 * [NullableCustomGenericPreferenceItem] that stores a nullable [List] inside one JSON array
 * string using caller-supplied element serializers.
 */
internal class NullableSerializedListPrimitive<T>(
    datastore: DataStore<Preferences>,
    key: String,
    elementSerializer: (T) -> String,
    elementDeserializer: (String) -> T,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NullableCustomGenericPreferenceItem<List<T>>(
    datastore = datastore,
    key = key,
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
