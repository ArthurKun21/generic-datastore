package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.core.PreferenceDefaults
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize

internal fun <T, F> nullableSerializedListFieldInternal(
    datastore: DataStore<T>,
    key: String,
    elementSerializer: (F) -> String,
    elementDeserializer: (String) -> F,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
): ProtoSerialFieldPreference<T, List<F>?> {
    val jsonInstance = PreferenceDefaults.defaultJson
    return ProtoSerialFieldPreference(
        datastore = datastore,
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
        defaultProtoValue = defaultProtoValue,
    )
}
