package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference

internal fun <T, F> serializedListFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: List<F>,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, List<F>> {
    val jsonInstance = PreferenceDefaults.defaultJson
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
                    val jsonArray = jsonInstance.decodeFromString<List<String>>(rawStr)
                    jsonArray.map { elementDeserializer(it) }
                }
            }
        },
        updater = { proto, value ->
            val jsonArray = value.map { elementSerializer(it) }
            updater(proto, jsonInstance.encodeToString(jsonArray))
        },
        defaultProtoValue = defaultProtoValue,
    )
}
