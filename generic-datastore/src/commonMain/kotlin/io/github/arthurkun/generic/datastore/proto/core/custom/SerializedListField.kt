package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference

internal fun <T, F> ProtoDatastore<T>.serializedListFieldInternal(
    key: String,
    defaultValue: List<F>,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<List<F>> {
    val jsonInstance = PreferenceDefaults.defaultJson
    return field(
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
    )
}
