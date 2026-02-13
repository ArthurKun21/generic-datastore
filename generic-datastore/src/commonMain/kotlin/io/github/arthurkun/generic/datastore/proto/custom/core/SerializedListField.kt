package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.json.Json

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
                    jsonArray.map { elementDeserializer(it) }
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
