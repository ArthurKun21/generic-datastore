package io.github.arthurkun.generic.datastore.proto.custom.optional

import androidx.datastore.core.DataStore
import io.github.arthurkun.generic.datastore.proto.custom.ProtoSerialFieldPreference
import io.github.arthurkun.generic.datastore.proto.custom.core.safeDeserialize
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal fun <T, K, V> nullableSerializedMapFieldInternal(
    datastore: DataStore<T>,
    key: String,
    keySerializer: (K) -> String,
    keyDeserializer: (String) -> K,
    valueSerializer: (V) -> String,
    valueDeserializer: (String) -> V,
    getter: (T) -> String?,
    updater: (T, String?) -> T,
    defaultProtoValue: T,
    json: Json,
    onDecodeFailure: ((String, Throwable) -> Unit)? = null,
): ProtoSerialFieldPreference<T, Map<K, V>?> {
    return ProtoSerialFieldPreference(
        datastore = datastore,
        key = key,
        defaultValue = null,
        getter = { proto ->
            val raw = getter(proto)
            raw?.let {
                safeDeserialize<Map<K, V>?>(it, null) { rawStr ->
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
            updater(
                proto,
                value?.let { map ->
                    val rawMap = map.map { (k, v) -> keySerializer(k) to valueSerializer(v) }.toMap()
                    json.encodeToString(rawMap)
                },
            )
        },
        defaultProtoValue = defaultProtoValue,
    )
}
