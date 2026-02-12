package io.github.arthurkun.generic.datastore.proto.optional.custom

import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.core.custom.safeDeserialize

internal fun <T, F> ProtoDatastore<T>.nullableSerializedListFieldInternal(
    key: String,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
): ProtoPreference<List<F>?> {
    val jsonInstance = PreferenceDefaults.defaultJson
    return field(
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let {
                safeDeserialize<List<F>?>(it, null) { rawStr ->
                    val jsonArray = jsonInstance.decodeFromString<List<String>>(rawStr)
                    jsonArray.map { elem -> elementDeserializer(elem) }
                }
            }
        },
        updater = { proto, value ->
            updater(
                proto,
                value?.let { list ->
                    val jsonArray = list.map { elementSerializer(it) }
                    jsonInstance.encodeToString(jsonArray)
                },
            )
        },
    )
}
