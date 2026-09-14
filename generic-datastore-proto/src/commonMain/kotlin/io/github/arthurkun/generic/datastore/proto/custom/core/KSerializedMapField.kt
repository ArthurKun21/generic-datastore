package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json

internal fun <T, K, V> kserializedMapFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: Map<K, V>,
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
    json: Json,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
): ProtoSerialFieldPreference<T, Map<K, V>> {
    val mapSerializer = MapSerializer(keySerializer, valueSerializer)
    return ProtoSerialFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) {
                defaultValue
            } else {
                safeDeserialize(
                    raw,
                    defaultValue,
                    onDecodeFailure?.let { callback ->
                        { error: Throwable -> callback(key, error) }
                    },
                ) { json.decodeFromString(mapSerializer, it) }
            }
        },
        updater = { proto, value ->
            updater(proto, json.encodeToString(mapSerializer, value))
        },
        defaultProtoValue = defaultProtoValue,
    )
}
