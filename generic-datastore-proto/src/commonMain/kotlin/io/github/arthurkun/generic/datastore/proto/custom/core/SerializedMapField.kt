package io.github.arthurkun.generic.datastore.proto.custom.core

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal fun <T, K, V> serializedMapFieldInternal(
    datastore: DataStore<T>,
    key: String,
    defaultValue: Map<K, V>,
    keySerializer: (K) -> String,
    keyDeserializer: (String) -> K,
    valueSerializer: (V) -> String,
    valueDeserializer: (String) -> V,
    getter: (T) -> String,
    updater: (T, String) -> T,
    defaultProtoValue: T,
    json: Json,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
): ProtoSerialFieldPreference<T, Map<K, V>> {
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
                    val rawMap = json.decodeFromString<Map<String, String>>(rawStr)
                    val entries = mutableMapOf<K, V>()
                    rawMap.forEach { (rawKey, rawValue) ->
                        try {
                            entries[keyDeserializer(rawKey)] = valueDeserializer(rawValue)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            onDecodeFailure?.invoke(key, e)
                            // Skip only entries that failed to deserialize.
                        }
                    }
                    entries
                }
            }
        },
        updater = { proto, value ->
            val rawMap = value.map { (k, v) -> keySerializer(k) to valueSerializer(v) }.toMap()
            updater(proto, json.encodeToString(rawMap))
        },
        defaultProtoValue = defaultProtoValue,
    )
}
