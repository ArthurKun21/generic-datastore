package io.github.arthurkun.generic.datastore.proto.core.custom

import io.github.arthurkun.generic.datastore.proto.ProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal fun <T, F> ProtoDatastore<T>.kserializedFieldInternal(
    key: String,
    defaultValue: F,
    serializer: KSerializer<F>,
    json: Json?,
    getter: (T) -> String,
    updater: (T, String) -> T,
): ProtoPreference<F> {
    val jsonInstance = resolveJson(json)
    return field(
        key = key,
        defaultValue = defaultValue,
        getter = { proto ->
            val raw = getter(proto)
            if (raw.isBlank()) {
                defaultValue
            } else {
                safeDeserialize(raw, defaultValue) { jsonInstance.decodeFromString(serializer, it) }
            }
        },
        updater = { proto, value ->
            updater(proto, jsonInstance.encodeToString(serializer, value))
        },
    )
}
