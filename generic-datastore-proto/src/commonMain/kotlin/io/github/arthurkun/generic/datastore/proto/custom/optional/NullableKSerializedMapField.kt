package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json

internal fun <T, K, V> nullableKserializedMapFieldInternal(
    datastore: DataStore<T>,
    key: String,
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
    json: Json,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
): ProtoSerialFieldPreference<T, Map<K, V>?> {
    val mapSerializer = MapSerializer(keySerializer, valueSerializer)
    return ProtoSerialFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let {
                safeDeserialize<Map<K, V>?>(
                    it,
                    null,
                    onDecodeFailure?.let { callback ->
                        { error: Throwable -> callback(key, error) }
                    },
                ) { s -> json.decodeFromString(mapSerializer, s) }
            }
        },
        updater = { proto, value ->
            updater(proto, value?.let { json.encodeToString(mapSerializer, it) })
        },
        defaultProtoValue = defaultProtoValue,
    )
}
