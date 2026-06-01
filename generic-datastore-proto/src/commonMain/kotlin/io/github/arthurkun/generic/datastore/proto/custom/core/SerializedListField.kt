package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal fun <T, F> serializedListFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: List<F>,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
    json: Json,
): ProtoSerialFieldPreference<T, List<F>> {
    return ProtoSerialFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) {
                defaultValue
            } else {
                safeDeserialize(raw, defaultValue) { rawStr ->
                    val jsonArray = json.decodeFromString<List<String>>(rawStr)
                    val elements = mutableListOf<F>()
                    jsonArray.forEach { element ->
                        try {
                            elements.add(elementDeserializer(element))
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Skip only elements that failed to deserialize.
                        }
                    }
                    elements
                }
            }
        },
        updater = { proto, value ->
            val jsonArray = value.map { elementSerializer(it) }
            updater(proto, json.encodeToString(jsonArray))
        },
        defaultProtoValue = defaultProtoValue,
    )
}
